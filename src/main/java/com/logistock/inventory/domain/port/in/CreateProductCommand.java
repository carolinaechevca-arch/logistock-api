package com.logistock.inventory.domain.port.in;

import com.logistock.inventory.domain.enums.ProductCategory;

import java.math.BigDecimal;

public record CreateProductCommand(
        String name,
        String description,
        ProductCategory category,
        int stock,
        BigDecimal price
) {
}
