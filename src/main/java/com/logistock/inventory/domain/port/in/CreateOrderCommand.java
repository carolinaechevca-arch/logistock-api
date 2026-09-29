package com.logistock.inventory.domain.port.in;

import java.util.List;

public record CreateOrderCommand(List<CreateOrderItemCommand> items) {
}
