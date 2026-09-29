package com.logistock.inventory.domain.port.in;

import com.logistock.inventory.domain.model.Order;

public interface CreateOrderUseCase {

    Order create(CreateOrderCommand command);
}
