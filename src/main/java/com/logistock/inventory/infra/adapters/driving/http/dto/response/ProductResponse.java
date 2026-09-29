package com.logistock.inventory.infra.adapters.driving.http.dto.response;

import com.logistock.inventory.domain.enums.ProductCategory;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String name,
        String description,
        ProductCategory category,
        int stock,
        BigDecimal price,
        Instant createdAt,
        Instant updatedAt
) {
}
