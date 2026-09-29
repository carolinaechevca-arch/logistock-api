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
                .andExpect(jsonPath("$.paths['/api/v1/inventory/entries'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/inventory/exits'].post").exists());
    }

    @Test
    void registersInventoryExitAndDecreasesStock() throws Exception {
        Long productId = createProduct();

        mockMvc.perform(post("/api/v1/inventory/exits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "quantity": 5,
                                  "observation": "Customer shipment"
                                }
                                """.formatted(productId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.type").value("EXIT"))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.observation").value("Customer shipment"));

        assertThat(productRepository.findById(productId).orElseThrow().getStock()).isEqualTo(7);
        assertThat(movementRepository.count()).isEqualTo(1);
    }

    @Test
    void rejectsExitGreaterThanAvailableStock() throws Exception {
        Long productId = createProduct();

        mockMvc.perform(post("/api/v1/inventory/exits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "quantity": 13
                                }
                                """.formatted(productId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"))
                .andExpect(jsonPath("$.message").value(
                        "Insufficient stock for product %d. Available: 12, requested: 13"
                                .formatted(productId)
                ));

        assertThat(productRepository.findById(productId).orElseThrow().getStock()).isEqualTo(12);
        assertThat(movementRepository.count()).isZero();
    }

    @Test
    void rejectsNonPositiveExitQuantity() throws Exception {
        Long productId = createProduct();

        mockMvc.perform(post("/api/v1/inventory/exits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "quantity": 0
                                }
                                """.formatted(productId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        assertThat(productRepository.findById(productId).orElseThrow().getStock()).isEqualTo(12);
        assertThat(movementRepository.count()).isZero();
    }

    @Test
    void returnsNotFoundForExitFromMissingProduct() throws Exception {
        mockMvc.perform(post("/api/v1/inventory/exits")
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
