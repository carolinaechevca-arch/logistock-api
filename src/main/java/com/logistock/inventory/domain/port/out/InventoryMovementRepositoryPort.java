package com.logistock.inventory.domain.port.out;

import com.logistock.inventory.domain.model.InventoryMovement;
import com.logistock.inventory.domain.model.PageResult;
import com.logistock.inventory.domain.model.PaginationCriteria;

public interface InventoryMovementRepositoryPort {

    InventoryMovement save(InventoryMovement movement);

    PageResult<InventoryMovement> findAll(PaginationCriteria criteria);

    PageResult<InventoryMovement> findByProductId(
            Long productId,
            PaginationCriteria criteria
    );
}
