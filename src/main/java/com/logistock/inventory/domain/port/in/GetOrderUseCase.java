package com.logistock.inventory.domain.port.in;

import com.logistock.inventory.domain.model.Order;

public interface GetOrderUseCase {

    Order getById(Long id);
}
