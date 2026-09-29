package com.logistock.inventory.domain.usecase;

import com.logistock.inventory.domain.model.InventoryMovement;
import com.logistock.inventory.domain.model.PageResult;
import com.logistock.inventory.domain.model.PaginationCriteria;
import com.logistock.inventory.domain.port.in.ListInventoryMovementsUseCase;
import com.logistock.inventory.domain.port.out.InventoryMovementRepositoryPort;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
public class ListInventoryMovementsService implements ListInventoryMovementsUseCase {

    @NonNull
    private final InventoryMovementRepositoryPort movementRepository;

    @Override
    public PageResult<InventoryMovement> list(PaginationCriteria criteria) {
        Objects.requireNonNull(criteria);
        PaginationRules.validate(criteria.page(), criteria.size());
        return movementRepository.findAll(criteria);
    }
}
