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
                .andExpect(jsonPath("$.paths['/api/v1/inventory/exits'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/inventory/movements'].get").exists())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/inventory/movements/product/{productId}'].get"
                ).exists());
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

    @Test
    void listsInventoryMovementsFromNewestToOldest() throws Exception {
        Long productId = createProduct();
        registerEntry(productId, 8);
        registerExit(productId, 5);

        mockMvc.perform(get("/api/v1/inventory/movements")
                        .queryParam("page", "0")
                        .queryParam("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].type").value("EXIT"))
                .andExpect(jsonPath("$.content[0].productId").value(productId))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void rejectsInvalidMovementPagination() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/movements")
                        .queryParam("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PAGINATION"));
    }

    @Test
    void listsOnlyMovementsForRequestedProduct() throws Exception {
        Long firstProductId = createProduct("Barcode scanner");
        Long secondProductId = createProduct("Warehouse tablet");
        registerEntry(firstProductId, 8);
        registerEntry(secondProductId, 3);
        registerExit(firstProductId, 5);

        mockMvc.perform(get(
                        "/api/v1/inventory/movements/product/{productId}",
                        firstProductId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].type").value("EXIT"))
                .andExpect(jsonPath("$.content[0].productId").value(firstProductId))
                .andExpect(jsonPath("$.content[1].type").value("ENTRY"))
                .andExpect(jsonPath("$.content[1].productId").value(firstProductId))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void returnsEmptyPageForProductWithoutMovements() throws Exception {
        Long productId = createProduct("Product without movements");

        mockMvc.perform(get(
                        "/api/v1/inventory/movements/product/{productId}",
                        productId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void returnsNotFoundWhenListingMovementsForMissingProduct() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/movements/product/{productId}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void rejectsInvalidProductIdWhenListingMovements() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/movements/product/{productId}", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PRODUCT_ID"));
    }

    private Long createProduct() throws Exception {
        return createProduct("Barcode scanner");
    }

    private Long createProduct(String name) throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "description": "Warehouse device",
                                  "category": "ELECTRONICS",
                                  "stock": 12,
                                  "price": 245.90
                                }
                                """.formatted(name)))
                .andExpect(status().isCreated());
        return productRepository.findAll().stream()
                .map(product -> product.getId())
                .max(Long::compareTo)
                .orElseThrow();
    }

    private void registerEntry(Long productId, int quantity) throws Exception {
        mockMvc.perform(post("/api/v1/inventory/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "quantity": %d
                                }
                                """.formatted(productId, quantity)))
                .andExpect(status().isCreated());
    }

    private void registerExit(Long productId, int quantity) throws Exception {
        mockMvc.perform(post("/api/v1/inventory/exits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "quantity": %d
                                }
                                """.formatted(productId, quantity)))
                .andExpect(status().isCreated());
    }
}
