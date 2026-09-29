package com.logistock.inventory.infra.adapters.driving.http.controller;

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
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpringDataProductRepository productRepository;

    @Test
    void createsProduct() throws Exception {
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
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        matchesPattern("http://localhost/api/v1/products/\\d+")
                ))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Barcode scanner"))
                .andExpect(jsonPath("$.category").value("ELECTRONICS"))
                .andExpect(jsonPath("$.stock").value(12))
                .andExpect(jsonPath("$.price").value(245.90));

        assertThat(productRepository.count()).isEqualTo(1);
    }

    @Test
    void rejectsInvalidProduct() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": " ",
                                  "category": "FOOD",
                                  "stock": -1,
                                  "price": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value("/api/v1/products"))
                .andExpect(jsonPath("$.details.name").exists())
                .andExpect(jsonPath("$.details.stock").exists())
                .andExpect(jsonPath("$.details.price").exists());

        assertThat(productRepository.count()).isZero();
    }

    @Test
    void exposesCreateProductInOpenApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/products'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/products'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v1/products/{id}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v1/products/{id}'].delete").exists())
                .andExpect(jsonPath("$.paths['/api/v1/products/restock'].get").exists());
    }

    @Test
    void getsProductById() throws Exception {
        createProduct();
        Long productId = productRepository.findAll().getFirst().getId();

        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Barcode scanner"))
                .andExpect(jsonPath("$.category").value("ELECTRONICS"))
                .andExpect(jsonPath("$.stock").value(12))
                .andExpect(jsonPath("$.price").value(245.90));
    }

    @Test
    void returnsNotFoundWhenProductDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Product not found with id 999999"))
                .andExpect(jsonPath("$.path").value("/api/v1/products/999999"));
    }

    @Test
    void rejectsInvalidProductId() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PRODUCT_ID"))
                .andExpect(jsonPath("$.message").value("Product id must be greater than zero"));
    }

    @Test
    void listsProductsWithPagination() throws Exception {
        createProduct("Scanner", "ELECTRONICS", 12);
        createProduct("Laptop", "ELECTRONICS", 8);
        createProduct("Chair", "HOME", 20);

        mockMvc.perform(get("/api/v1/products")
                        .queryParam("page", "0")
                        .queryParam("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void filtersProductsByCategoryAndStockRange() throws Exception {
        createProduct("Scanner", "ELECTRONICS", 5);
        createProduct("Laptop", "ELECTRONICS", 20);
        createProduct("Rice", "FOOD", 15);

        mockMvc.perform(get("/api/v1/products")
                        .queryParam("category", "ELECTRONICS")
                        .queryParam("minStock", "10")
                        .queryParam("maxStock", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Laptop"))
                .andExpect(jsonPath("$.content[0].stock").value(20))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void rejectsInvalidStockRange() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .queryParam("minStock", "20")
                        .queryParam("maxStock", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_STOCK_RANGE"))
                .andExpect(jsonPath("$.message")
                        .value("minStock must be less than or equal to maxStock"));
    }

    @Test
    void rejectsInvalidPagination() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .queryParam("page", "-1")
                        .queryParam("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PAGINATION"));
    }

    @Test
    void deletesProductWithZeroStock() throws Exception {
        createProduct("Empty box", "OTHER", 0);
        Long productId = productRepository.findAll().getFirst().getId();

        mockMvc.perform(delete("/api/v1/products/{id}", productId))
                .andExpect(status().isNoContent());

        assertThat(productRepository.findById(productId)).isEmpty();
    }

    @Test
    void rejectsDeletionWhenProductHasStock() throws Exception {
        createProduct("Scanner", "ELECTRONICS", 5);
        Long productId = productRepository.findAll().getFirst().getId();

        mockMvc.perform(delete("/api/v1/products/{id}", productId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRODUCT_HAS_STOCK"))
                .andExpect(jsonPath("$.message").value(
                        "Product %d cannot be deleted because it has 5 units in stock"
                                .formatted(productId)
                ));

        assertThat(productRepository.findById(productId)).isPresent();
    }

    @Test
    void returnsNotFoundWhenDeletingMissingProduct() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void rejectsInvalidProductIdWhenDeleting() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{id}", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PRODUCT_ID"));
    }

    @Test
    void listsProductsThatRequireRestocking() throws Exception {
        createProduct("Empty box", "OTHER", 0);
        createProduct("Low stock scanner", "ELECTRONICS", 4);
        createProduct("Stock threshold", "FOOD", 5);
        createProduct("Available chair", "HOME", 20);

        mockMvc.perform(get("/api/v1/products/restock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Empty box"))
                .andExpect(jsonPath("$.content[0].stock").value(0))
                .andExpect(jsonPath("$.content[1].name").value("Low stock scanner"))
                .andExpect(jsonPath("$.content[1].stock").value(4))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void paginatesProductsThatRequireRestocking() throws Exception {
        createProduct("Empty box", "OTHER", 0);
        createProduct("Low stock scanner", "ELECTRONICS", 4);

        mockMvc.perform(get("/api/v1/products/restock")
                        .queryParam("page", "0")
                        .queryParam("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void rejectsInvalidRestockPagination() throws Exception {
        mockMvc.perform(get("/api/v1/products/restock")
                        .queryParam("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PAGINATION"));
    }

    private void createProduct() throws Exception {
        createProduct("Barcode scanner", "ELECTRONICS", 12);
    }

    private void createProduct(String name, String category, int stock) throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "description": "Warehouse device",
                                  "category": "%s",
                                  "stock": %d,
                                  "price": 245.90
                                }
                                """.formatted(name, category, stock)))
                .andExpect(status().isCreated());
    }
}
