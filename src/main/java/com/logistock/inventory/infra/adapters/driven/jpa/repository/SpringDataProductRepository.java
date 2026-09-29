package com.logistock.inventory.infra.adapters.driven.jpa.repository;

import com.logistock.inventory.infra.adapters.driven.jpa.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataProductRepository extends JpaRepository<ProductEntity, Long> {
}
