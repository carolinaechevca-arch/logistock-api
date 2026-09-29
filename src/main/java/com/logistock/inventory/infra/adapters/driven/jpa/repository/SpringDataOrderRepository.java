package com.logistock.inventory.infra.adapters.driven.jpa.repository;

import com.logistock.inventory.infra.adapters.driven.jpa.entity.OrderEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataOrderRepository extends JpaRepository<OrderEntity, Long> {

    @Override
    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<OrderEntity> findById(Long id);
}
