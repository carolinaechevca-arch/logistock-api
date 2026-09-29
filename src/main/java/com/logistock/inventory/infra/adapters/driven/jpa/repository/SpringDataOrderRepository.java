package com.logistock.inventory.infra.adapters.driven.jpa.repository;

import com.logistock.inventory.infra.adapters.driven.jpa.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataOrderRepository extends JpaRepository<OrderEntity, Long> {
}
