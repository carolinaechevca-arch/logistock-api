package com.logistock.inventory.domain.port.out;

import com.logistock.inventory.domain.model.Order;

public interface OrderRepositoryPort {

    Order save(Order order);
}
