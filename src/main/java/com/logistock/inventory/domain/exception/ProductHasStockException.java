package com.logistock.inventory.domain.exception;

import com.logistock.inventory.domain.constants.ProductConstants;
import lombok.Getter;

@Getter
public class ProductHasStockException extends RuntimeException {

    private final String code = ProductConstants.PRODUCT_HAS_STOCK;

    public ProductHasStockException(Long id, int stock) {
        super(ProductConstants.PRODUCT_HAS_STOCK_MESSAGE.formatted(id, stock));
    }
}
