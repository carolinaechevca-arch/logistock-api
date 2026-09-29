package com.logistock.inventory.domain.exception;

import com.logistock.inventory.domain.constants.InventoryConstants;
import lombok.Getter;

@Getter
public class InsufficientStockException extends RuntimeException {

    private final String code = InventoryConstants.INSUFFICIENT_STOCK;

    public InsufficientStockException(Long productId, int available, int requested) {
        super(InventoryConstants.INSUFFICIENT_STOCK_MESSAGE.formatted(
                productId,
                available,
                requested
        ));
    }
}
