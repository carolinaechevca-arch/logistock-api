package com.logistock.inventory.domain.usecase;

import com.logistock.inventory.domain.model.Order;
import com.logistock.inventory.domain.model.PageResult;
import com.logistock.inventory.domain.model.PaginationCriteria;
import com.logistock.inventory.domain.port.in.ListOrdersUseCase;
import com.logistock.inventory.domain.port.out.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
public class ListOrdersService implements ListOrdersUseCase {

    private final OrderRepositoryPort orderRepository;

    @Override
    public PageResult<Order> list(PaginationCriteria criteria) {
        Objects.requireNonNull(criteria);
        PaginationRules.validate(criteria.page(), criteria.size());
        return orderRepository.findAll(criteria);
    }
}
