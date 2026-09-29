package com.logistock.inventory.domain.model;

import java.util.List;

public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {

    public PageResult {
        content = List.copyOf(content);
    }
}
