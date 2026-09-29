package com.logistock.inventory.domain.exception;

import com.logistock.inventory.domain.constants.ProductConstants;
import lombok.Getter;

@Getter
public class ProductNotFoundException extends RuntimeException {

    private final String code = ProductConstants.PRODUCT_NOT_FOUND;

    public ProductNotFoundException(Long id) {
        super(ProductConstants.PRODUCT_NOT_FOUND_MESSAGE.formatted(id));
    }
}
