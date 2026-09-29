package com.logistock.inventory.infra.adapters.driven.jpa.repository;

import com.logistock.inventory.infra.adapters.driven.jpa.entity.InventoryMovementEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataInventoryMovementRepository extends
        JpaRepository<InventoryMovementEntity, Long> {

    Page<InventoryMovementEntity> findByProductId(Long productId, Pageable pageable);
}
