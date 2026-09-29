package com.logistock.inventory.infra.adapters.driving.http.controller;

import com.logistock.inventory.infra.adapters.driven.jpa.repository.SpringDataInventoryMovementRepository;
import com.logistock.inventory.infra.adapters.driven.jpa.repository.SpringDataProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class InventoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpringDataProductRepository productRepository;

    @Autowired
    private SpringDataInventoryMovementRepository movementRepository;

    @Test
    void registersInventoryEntryAndIncreasesStock() throws Exception {
        Long productId = createProduct();

        mockMvc.perform(post("/api/v1/inventory/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "quantity": 8,
                                  "observation": "Supplier delivery"
                                }
                                """.formatted(productId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.type").value("ENTRY"))
                .andExpect(jsonPath("$.quantity").value(8))
                .andExpect(jsonPath("$.observation").value("Supplier delivery"));

        assertThat(productRepository.findById(productId).orElseThrow().getStock()).isEqualTo(20);
        assertThat(movementRepository.count()).isEqualTo(1);
        assertThat(movementRepository.findAll().getFirst().getProduct().getId()).isEqualTo(productId);
    }

    @Test
    void rejectsNonPositiveEntryQuantity() throws Exception {
        Long productId = createProduct();

        mockMvc.perform(post("/api/v1/inventory/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "quantity": 0
                                }
                                """.formatted(productId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.quantity").exists());

        assertThat(productRepository.findById(productId).orElseThrow().getStock()).isEqualTo(12);
        assertThat(movementRepository.count()).isZero();
    }

    @Test
    void returnsNotFoundForMissingProduct() throws Exception {
        mockMvc.perform(post("/api/v1/inventory/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 999999,
                                  "quantity": 5
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));

        assertThat(movementRepository.count()).isZero();
    }

    @Test
    void exposesInventoryEntryInOpenApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/inventory/entries'].post").exists());
    }

    private Long createProduct() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Barcode scanner",
                                  "description": "Warehouse device",
                                  "category": "ELECTRONICS",
                                  "stock": 12,
                                  "price": 245.90
                                }
                                """))
                .andExpect(status().isCreated());
        return productRepository.findAll().getFirst().getId();
    }
}
