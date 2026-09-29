package com.logistock.inventory.domain.model;

import com.logistock.inventory.domain.constants.InventoryConstants;
import com.logistock.inventory.domain.enums.InventoryMovementType;
import com.logistock.inventory.domain.exception.InvalidInventoryMovementException;

import java.time.Instant;

public record InventoryMovement(
        Long id,
        Long productId,
        InventoryMovementType type,
        int quantity,
        Instant createdAt,
        String observation
) {

    public InventoryMovement {
        if (productId == null || productId <= 0) {
            throw new InvalidInventoryMovementException("A valid product id is required");
        }
        if (type == null) {
            throw new InvalidInventoryMovementException("Inventory movement type is required");
        }
        if (quantity < InventoryConstants.MINIMUM_MOVEMENT_QUANTITY) {
            throw new InvalidInventoryMovementException(
                    InventoryConstants.INVALID_MOVEMENT_QUANTITY_MESSAGE
            );
        }
        if (createdAt == null) {
            throw new InvalidInventoryMovementException("Inventory movement date is required");
        }
        observation = normalizeObservation(observation);
    }

    public static InventoryMovement entry(
            Long productId,
            int quantity,
            Instant createdAt,
            String observation
    ) {
        return new InventoryMovement(
                null,
                productId,
                InventoryMovementType.ENTRY,
                quantity,
                createdAt,
                observation
        );
    }

    private static String normalizeObservation(String observation) {
        if (observation == null || observation.isBlank()) {
            return null;
        }
        String normalized = observation.trim();
        if (normalized.length() > InventoryConstants.OBSERVATION_MAX_LENGTH) {
            throw new InvalidInventoryMovementException("Inventory observation is too long");
        }
        return normalized;
    }
}
