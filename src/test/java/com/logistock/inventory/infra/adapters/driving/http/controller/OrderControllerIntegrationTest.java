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
                .andExpect(jsonPath("$.paths['/api/v1/orders'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/orders'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v1/orders/{id}'].get").exists());
    }

    @Test
    void getsOrderByIdWithItsItems() throws Exception {
        Long productId = createProduct("Barcode scanner", 12);
        Long orderId = createOrder(productId, 3);

        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").isNumber())
                .andExpect(jsonPath("$.items[0].productId").value(productId))
                .andExpect(jsonPath("$.items[0].quantity").value(3));
    }

    @Test
    void returnsNotFoundForMissingOrder() throws Exception {
        mockMvc.perform(get("/api/v1/orders/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Order not found with id 999999"));
    }

    @Test
    void rejectsInvalidOrderId() throws Exception {
        mockMvc.perform(get("/api/v1/orders/{id}", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ORDER_ID"));
    }

    @Test
    void listsOrdersFromNewestToOldest() throws Exception {
        Long firstProductId = createProduct("Barcode scanner", 12);
        Long secondProductId = createProduct("Warehouse tablet", 7);
        createOrder(firstProductId, 3);
        Long newestOrderId = createOrder(secondProductId, 2);

        mockMvc.perform(get("/api/v1/orders")
                        .queryParam("page", "0")
                        .queryParam("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(newestOrderId))
                .andExpect(jsonPath("$.content[0].status").value("CREATED"))
                .andExpect(jsonPath("$.content[0].items.length()").value(1))
                .andExpect(jsonPath("$.content[0].items[0].productId").value(secondProductId))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void returnsEmptyOrderPage() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void rejectsInvalidOrderPagination() throws Exception {
        mockMvc.perform(get("/api/v1/orders").queryParam("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PAGINATION"));
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

    private Long createOrder(Long productId, int quantity) throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    {"productId": %d, "quantity": %d}
                                  ]
                                }
                                """.formatted(productId, quantity)))
                .andExpect(status().isCreated());
        return orderRepository.findAll().stream()
                .map(OrderEntity::getId)
                .max(Long::compareTo)
                .orElseThrow();
    }
}
