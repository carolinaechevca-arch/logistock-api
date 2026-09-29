package com.logistock.inventory.domain.port.out;

import com.logistock.inventory.domain.model.Order;
import com.logistock.inventory.domain.model.PageResult;
import com.logistock.inventory.domain.model.PaginationCriteria;

import java.util.Optional;

public interface OrderRepositoryPort {

    Order save(Order order);

    Optional<Order> findById(Long id);

    PageResult<Order> findAll(PaginationCriteria criteria);
}
