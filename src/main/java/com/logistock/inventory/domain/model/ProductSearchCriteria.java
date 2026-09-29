package com.logistock.inventory.domain.model;

import com.logistock.inventory.domain.enums.ProductCategory;

public record ProductSearchCriteria(
        ProductCategory category,
        Integer minStock,
        Integer maxStock,
        int page,
        int size
) {
}
