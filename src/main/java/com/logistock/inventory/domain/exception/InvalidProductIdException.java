package com.logistock.inventory.domain.exception;

import com.logistock.inventory.domain.constants.ProductConstants;
import lombok.Getter;

@Getter
public class InvalidProductIdException extends RuntimeException {

    private final String code = ProductConstants.INVALID_PRODUCT_ID;

    public InvalidProductIdException() {
        super(ProductConstants.INVALID_PRODUCT_ID_MESSAGE);
    }
}
