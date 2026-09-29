package com.logistock.inventory.infra.adapters.driving.http.dto.response;

import com.logistock.inventory.domain.enums.OrderStatus;

import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        Instant createdAt,
        OrderStatus status,
        List<OrderItemResponse> items
) {
}
