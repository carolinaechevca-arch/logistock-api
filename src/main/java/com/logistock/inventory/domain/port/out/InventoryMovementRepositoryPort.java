package com.logistock.inventory.domain.port.out;

import com.logistock.inventory.domain.model.InventoryMovement;

public interface InventoryMovementRepositoryPort {

    InventoryMovement save(InventoryMovement movement);
}
