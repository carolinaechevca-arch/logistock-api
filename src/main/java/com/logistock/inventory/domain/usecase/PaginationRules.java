package com.logistock.inventory.domain.usecase;

import com.logistock.inventory.domain.constants.ProductConstants;
import com.logistock.inventory.domain.exception.InvalidPaginationException;

public final class PaginationRules {

    private PaginationRules() {
    }

    public static void validate(int page, int size) {
        if (page < ProductConstants.DEFAULT_PAGE
                || size < 1
                || size > ProductConstants.MAX_PAGE_SIZE) {
            throw new InvalidPaginationException();
        }
    }
}
