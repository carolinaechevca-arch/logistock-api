package com.logistock.inventory.domain.usecase;

import com.logistock.inventory.domain.constants.ProductConstants;
import com.logistock.inventory.domain.exception.InvalidPaginationException;
import com.logistock.inventory.domain.exception.InvalidStockRangeException;
import com.logistock.inventory.domain.model.PageResult;
import com.logistock.inventory.domain.model.Product;
import com.logistock.inventory.domain.model.ProductSearchCriteria;
import com.logistock.inventory.domain.port.in.ListProductsUseCase;
import com.logistock.inventory.domain.port.out.ProductRepositoryPort;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
public class ListProductsService implements ListProductsUseCase {

    @NonNull
    private final ProductRepositoryPort productRepository;

    @Override
    public PageResult<Product> list(ProductSearchCriteria criteria) {
        Objects.requireNonNull(criteria);
        validatePagination(criteria);
        validateStockRange(criteria);
        return productRepository.findAll(criteria);
    }

    private void validatePagination(ProductSearchCriteria criteria) {
        if (criteria.page() < ProductConstants.DEFAULT_PAGE
                || criteria.size() < 1
                || criteria.size() > ProductConstants.MAX_PAGE_SIZE) {
            throw new InvalidPaginationException();
        }
    }

    private void validateStockRange(ProductSearchCriteria criteria) {
        if (isNegative(criteria.minStock()) || isNegative(criteria.maxStock())) {
            throw new InvalidStockRangeException(ProductConstants.NEGATIVE_STOCK_FILTER_MESSAGE);
        }
        if (criteria.minStock() != null
                && criteria.maxStock() != null
                && criteria.minStock() > criteria.maxStock()) {
            throw new InvalidStockRangeException(ProductConstants.INVALID_STOCK_RANGE_MESSAGE);
        }
    }

    private boolean isNegative(Integer stock) {
        return stock != null && stock < ProductConstants.MINIMUM_STOCK;
    }
}
