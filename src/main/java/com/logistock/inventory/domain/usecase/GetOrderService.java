package com.logistock.inventory.domain.usecase;

import com.logistock.inventory.domain.exception.InvalidOrderIdException;
import com.logistock.inventory.domain.exception.OrderNotFoundException;
import com.logistock.inventory.domain.model.Order;
import com.logistock.inventory.domain.port.in.GetOrderUseCase;
import com.logistock.inventory.domain.port.out.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetOrderService implements GetOrderUseCase {

    private final OrderRepositoryPort orderRepository;

    @Override
    public Order getById(Long id) {
        if (id == null || id <= 0) {
            throw new InvalidOrderIdException();
        }
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
