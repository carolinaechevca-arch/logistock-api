package com.logistock.inventory.domain.model;

import com.logistock.inventory.domain.constants.OrderConstants;
import com.logistock.inventory.domain.enums.OrderStatus;
import com.logistock.inventory.domain.exception.InvalidOrderException;

import java.time.Instant;
import java.util.List;

public record Order(
        Long id,
        Instant createdAt,
        OrderStatus status,
        List<OrderItem> items
) {

    public Order {
        if (createdAt == null) {
            throw new InvalidOrderException(OrderConstants.CREATION_DATE_REQUIRED_MESSAGE);
        }
        if (status == null) {
            throw new InvalidOrderException(OrderConstants.STATUS_REQUIRED_MESSAGE);
        }
        if (items == null || items.isEmpty()) {
            throw new InvalidOrderException(OrderConstants.EMPTY_ORDER_MESSAGE);
        }
        items = List.copyOf(items);
    }

    public static Order create(List<OrderItem> items, Instant createdAt) {
        return new Order(null, createdAt, OrderStatus.CREATED, items);
    }
}
