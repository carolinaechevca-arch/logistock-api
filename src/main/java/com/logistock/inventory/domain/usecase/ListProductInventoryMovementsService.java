package com.logistock.inventory.domain.usecase;

import com.logistock.inventory.domain.exception.InvalidProductIdException;
import com.logistock.inventory.domain.exception.ProductNotFoundException;
import com.logistock.inventory.domain.model.InventoryMovement;
import com.logistock.inventory.domain.model.PageResult;
import com.logistock.inventory.domain.model.PaginationCriteria;
import com.logistock.inventory.domain.port.in.ListProductInventoryMovementsUseCase;
import com.logistock.inventory.domain.port.out.InventoryMovementRepositoryPort;
import com.logistock.inventory.domain.port.out.ProductRepositoryPort;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
public class ListProductInventoryMovementsService implements
        ListProductInventoryMovementsUseCase {

    @NonNull
    private final ProductRepositoryPort productRepository;

    @NonNull
    private final InventoryMovementRepositoryPort movementRepository;

    @Override
    public PageResult<InventoryMovement> list(Long productId, PaginationCriteria criteria) {
        Objects.requireNonNull(criteria);
        if (productId == null || productId <= 0) {
            throw new InvalidProductIdException();
        }
        PaginationRules.validate(criteria.page(), criteria.size());
        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return movementRepository.findByProductId(productId, criteria);
    }
}
