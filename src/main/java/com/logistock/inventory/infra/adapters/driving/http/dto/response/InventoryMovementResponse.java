package com.logistock.inventory.infra.adapters.driving.http.dto.response;

import com.logistock.inventory.domain.enums.InventoryMovementType;

import java.time.Instant;

public record InventoryMovementResponse(
        Long id,
        Long productId,
        InventoryMovementType type,
        int quantity,
        Instant createdAt,
        String observation
) {
}
