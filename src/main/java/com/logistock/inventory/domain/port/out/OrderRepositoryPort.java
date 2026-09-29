package com.logistock.inventory.domain.port.out;

import com.logistock.inventory.domain.model.Order;

import java.util.Optional;

public interface OrderRepositoryPort {

    Order save(Order order);

    Optional<Order> findById(Long id);
}
