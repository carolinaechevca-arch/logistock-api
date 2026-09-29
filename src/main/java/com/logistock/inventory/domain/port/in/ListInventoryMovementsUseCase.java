package com.logistock.inventory.domain.port.in;

import com.logistock.inventory.domain.model.InventoryMovement;
import com.logistock.inventory.domain.model.PageResult;
import com.logistock.inventory.domain.model.PaginationCriteria;

public interface ListInventoryMovementsUseCase {

    PageResult<InventoryMovement> list(PaginationCriteria criteria);
}
