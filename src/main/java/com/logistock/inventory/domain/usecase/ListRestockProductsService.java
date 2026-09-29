package com.logistock.inventory.domain.usecase;

import com.logistock.inventory.domain.constants.ProductConstants;
import com.logistock.inventory.domain.model.PageResult;
import com.logistock.inventory.domain.model.PaginationCriteria;
import com.logistock.inventory.domain.model.Product;
import com.logistock.inventory.domain.model.ProductSearchCriteria;
import com.logistock.inventory.domain.port.in.ListRestockProductsUseCase;
import com.logistock.inventory.domain.port.out.ProductRepositoryPort;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
public class ListRestockProductsService implements ListRestockProductsUseCase {

    @NonNull
    private final ProductRepositoryPort productRepository;

    @Override
    public PageResult<Product> list(PaginationCriteria criteria) {
        Objects.requireNonNull(criteria);
        PaginationRules.validate(criteria.page(), criteria.size());
        ProductSearchCriteria searchCriteria = new ProductSearchCriteria(
                null,
                null,
                ProductConstants.RESTOCK_MAXIMUM_STOCK,
                criteria.page(),
                criteria.size()
        );
        return productRepository.findAll(searchCriteria);
    }
}
