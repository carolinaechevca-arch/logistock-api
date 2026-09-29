package com.logistock.inventory.domain.exception;

import com.logistock.inventory.domain.constants.ProductConstants;
import lombok.Getter;

@Getter
public class InvalidPaginationException extends RuntimeException {

    private final String code = ProductConstants.INVALID_PAGINATION;

    public InvalidPaginationException() {
        super(ProductConstants.INVALID_PAGINATION_MESSAGE);
    }
}
