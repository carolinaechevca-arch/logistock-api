package com.logistock.inventory.infra.adapters.driven.jpa.repository;

import com.logistock.inventory.domain.model.ProductSearchCriteria;
import com.logistock.inventory.infra.adapters.driven.jpa.constants.PersistenceConstants;
import com.logistock.inventory.infra.adapters.driven.jpa.entity.ProductEntity;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<ProductEntity> from(ProductSearchCriteria criteria) {
        Specification<ProductEntity> specification = (root, query, builder) -> builder.conjunction();
        if (criteria.category() != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(
                            root.get(PersistenceConstants.CATEGORY_ATTRIBUTE),
                            criteria.category()
                    ));
        }
        if (criteria.minStock() != null) {
            specification = specification.and((root, query, builder) ->
                    builder.greaterThanOrEqualTo(
                            root.get(PersistenceConstants.STOCK_ATTRIBUTE),
                            criteria.minStock()
                    ));
        }
        if (criteria.maxStock() != null) {
            specification = specification.and((root, query, builder) ->
                    builder.lessThanOrEqualTo(
                            root.get(PersistenceConstants.STOCK_ATTRIBUTE),
                            criteria.maxStock()
                    ));
        }
        return specification;
    }
}
