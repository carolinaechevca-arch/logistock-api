package com.logistock.inventory.domain.exception;

import com.logistock.inventory.domain.constants.ProductConstants;
import lombok.Getter;

@Getter
public class InvalidStockRangeException extends RuntimeException {

    private final String code = ProductConstants.INVALID_STOCK_RANGE;

    public InvalidStockRangeException(String message) {
        super(message);
    }
}
