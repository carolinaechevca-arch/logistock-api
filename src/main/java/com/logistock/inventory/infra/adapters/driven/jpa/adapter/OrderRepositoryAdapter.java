package com.logistock.inventory.infra.adapters.driven.jpa.adapter;

import com.logistock.inventory.domain.model.Order;
import com.logistock.inventory.domain.model.PageResult;
import com.logistock.inventory.domain.model.PaginationCriteria;
import com.logistock.inventory.infra.adapters.driven.jpa.constants.PersistenceConstants;
import com.logistock.inventory.infra.adapters.driven.jpa.entity.OrderEntity;
import com.logistock.inventory.infra.adapters.driven.jpa.entity.OrderItemEntity;
import com.logistock.inventory.infra.adapters.driven.jpa.entity.ProductEntity;
import com.logistock.inventory.infra.adapters.driven.jpa.mapper.OrderEntityMapper;
import com.logistock.inventory.infra.adapters.driven.jpa.repository.SpringDataOrderRepository;
import com.logistock.inventory.infra.adapters.driven.jpa.repository.SpringDataProductRepository;
import com.logistock.inventory.domain.port.out.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

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

    @Override
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public PageResult<Order> findAll(PaginationCriteria criteria) {
        Sort sort = Sort.by(
                Sort.Order.desc(PersistenceConstants.CREATED_AT_ATTRIBUTE),
                Sort.Order.desc(PersistenceConstants.ID_ATTRIBUTE)
        );
        Page<OrderEntity> result = orderRepository.findAll(
                PageRequest.of(criteria.page(), criteria.size(), sort)
        );
        return new PageResult<>(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }
}
