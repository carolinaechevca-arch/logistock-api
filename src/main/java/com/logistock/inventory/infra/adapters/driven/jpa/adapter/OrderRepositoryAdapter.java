package com.logistock.inventory.infra.adapters.driven.jpa.adapter;

import com.logistock.inventory.domain.model.Order;
import com.logistock.inventory.infra.adapters.driven.jpa.entity.OrderEntity;
import com.logistock.inventory.infra.adapters.driven.jpa.entity.OrderItemEntity;
import com.logistock.inventory.infra.adapters.driven.jpa.entity.ProductEntity;
import com.logistock.inventory.infra.adapters.driven.jpa.mapper.OrderEntityMapper;
import com.logistock.inventory.infra.adapters.driven.jpa.repository.SpringDataOrderRepository;
import com.logistock.inventory.infra.adapters.driven.jpa.repository.SpringDataProductRepository;
import com.logistock.inventory.domain.port.out.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final SpringDataOrderRepository orderRepository;
    private final SpringDataProductRepository productRepository;
    private final OrderEntityMapper mapper;

    @Override
    public Order save(Order order) {
        List<OrderItemEntity> items = order.items().stream()
                .map(item -> {
                    ProductEntity product = productRepository.getReferenceById(item.productId());
                    return mapper.toItemEntity(item, product);
                })
                .toList();
        OrderEntity savedOrder = orderRepository.save(mapper.toEntity(order, items));
        return mapper.toDomain(savedOrder);
    }
}
