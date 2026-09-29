package com.logistock.inventory.domain.port.in;

public record RegisterInventoryExitCommand(
        Long productId,
        int quantity,
        String observation
) {
}
