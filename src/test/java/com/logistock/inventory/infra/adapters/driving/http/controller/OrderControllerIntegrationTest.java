package com.logistock.inventory.infra.adapters.driving.http.controller;

import com.logistock.inventory.domain.enums.OrderStatus;
import com.logistock.inventory.infra.adapters.driven.jpa.entity.OrderEntity;
import com.logistock.inventory.infra.adapters.driven.jpa.repository.SpringDataOrderRepository;
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
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpringDataProductRepository productRepository;

    @Autowired
    private SpringDataOrderRepository orderRepository;

    @Test
    void createsOrderWithoutChangingStock() throws Exception {
        Long firstProductId = createProduct("Barcode scanner", 12);
        Long secondProductId = createProduct("Warehouse tablet", 7);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    {"productId": %d, "quantity": 3},
                                    {"productId": %d, "quantity": 2}
                                  ]
                                }
                                """.formatted(firstProductId, secondProductId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].productId").value(firstProductId))
                .andExpect(jsonPath("$.items[0].quantity").value(3))
                .andExpect(jsonPath("$.items[1].productId").value(secondProductId))
                .andExpect(jsonPath("$.items[1].quantity").value(2));

        assertThat(orderRepository.count()).isEqualTo(1);
        OrderEntity order = orderRepository.findAll().getFirst();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getItems()).hasSize(2);
        assertThat(productRepository.findById(firstProductId).orElseThrow().getStock())
                .isEqualTo(12);
        assertThat(productRepository.findById(secondProductId).orElseThrow().getStock())
                .isEqualTo(7);
    }

    @Test
    void rejectsOrderWithoutItems() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.items").exists());

        assertThat(orderRepository.count()).isZero();
    }

    @Test
    void rejectsDuplicateProduct() throws Exception {
        Long productId = createProduct("Barcode scanner", 12);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    {"productId": %d, "quantity": 1},
                                    {"productId": %d, "quantity": 2}
                                  ]
                                }
                                """.formatted(productId, productId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ORDER"));

        assertThat(orderRepository.count()).isZero();
    }

    @Test
    void returnsNotFoundForMissingProduct() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    {"productId": 999999, "quantity": 1}
                                  ]
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));

        assertThat(orderRepository.count()).isZero();
    }

    @Test
    void rejectsOrderQuantityGreaterThanAvailableStock() throws Exception {
        Long productId = createProduct("Barcode scanner", 2);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    {"productId": %d, "quantity": 3}
                                  ]
                                }
                                """.formatted(productId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));

        assertThat(orderRepository.count()).isZero();
        assertThat(productRepository.findById(productId).orElseThrow().getStock()).isEqualTo(2);
    }

    @Test
    void exposesOrderCreationInOpenApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/orders'].post").exists());
    }

    private Long createProduct(String name, int stock) throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "description": "Warehouse device",
                                  "category": "ELECTRONICS",
                                  "stock": %d,
                                  "price": 245.90
                                }
                                """.formatted(name, stock)))
                .andExpect(status().isCreated());
        return productRepository.findAll().stream()
                .map(product -> product.getId())
                .max(Long::compareTo)
                .orElseThrow();
    }
}
