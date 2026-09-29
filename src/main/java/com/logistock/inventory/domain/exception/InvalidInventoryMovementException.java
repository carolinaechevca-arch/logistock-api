package com.logistock.inventory.domain.exception;

import com.logistock.inventory.domain.constants.InventoryConstants;
import lombok.Getter;

@Getter
public class InvalidInventoryMovementException extends RuntimeException {

    private final String code = InventoryConstants.INVALID_INVENTORY_MOVEMENT;

    public InvalidInventoryMovementException(String message) {
        super(message);
    }
}
