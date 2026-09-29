package com.logistock.inventory.domain.port.in;

import com.logistock.inventory.domain.model.InventoryMovement;

public interface RegisterInventoryExitUseCase {

    InventoryMovement register(RegisterInventoryExitCommand command);
}
