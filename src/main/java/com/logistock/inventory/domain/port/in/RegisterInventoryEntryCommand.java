package com.logistock.inventory.domain.port.in;

public record RegisterInventoryEntryCommand(
        Long productId,
        int quantity,
        String observation
) {
}
