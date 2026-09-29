package com.logistock.inventory.domain.model;

import com.logistock.inventory.domain.enums.OrderStatus;
import com.logistock.inventory.domain.exception.InvalidOrderException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static final Instant NOW = Instant.parse("2026-09-28T20:00:00Z");

    @Test
    void createsOrderWithCreatedStatus() {
        OrderItem item = OrderItem.create(10L, 3);

        Order order = Order.create(List.of(item), NOW);

        assertThat(order.id()).isNull();
        assertThat(order.createdAt()).isEqualTo(NOW);
        assertThat(order.status()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.items()).containsExactly(item);
    }

    @Test
    void rejectsOrderWithoutItems() {
        assertThatThrownBy(() -> Order.create(List.of(), NOW))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessage("An order must contain at least one item");
    }

    @Test
    void rejectsOrderItemWithInvalidQuantity() {
        assertThatThrownBy(() -> OrderItem.create(10L, 0))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessage("Order item quantity must be greater than zero");
    }
}
