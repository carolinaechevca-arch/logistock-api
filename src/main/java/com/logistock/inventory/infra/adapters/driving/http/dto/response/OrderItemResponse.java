package com.logistock.inventory.infra.adapters.driving.http.dto.response;

public record OrderItemResponse(
        Long id,
        Long productId,
        int quantity
) {
}
