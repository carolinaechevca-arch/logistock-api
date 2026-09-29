package com.logistock.inventory.domain.port.in;

public record CreateOrderItemCommand(Long productId, int quantity) {
}
