package com.logistock.inventory.infra.adapters.driven.jpa.repository;

import com.logistock.inventory.infra.adapters.driven.jpa.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SpringDataProductRepository extends
        JpaRepository<ProductEntity, Long>,
        JpaSpecificationExecutor<ProductEntity> {
}
