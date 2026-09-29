package com.logistock.inventory.infra.adapters.driven.jpa.adapter;

import com.logistock.inventory.domain.model.InventoryMovement;
import com.logistock.inventory.domain.port.out.InventoryMovementRepositoryPort;
import com.logistock.inventory.infra.adapters.driven.jpa.entity.InventoryMovementEntity;
import com.logistock.inventory.infra.adapters.driven.jpa.entity.ProductEntity;
import com.logistock.inventory.infra.adapters.driven.jpa.mapper.InventoryMovementEntityMapper;
import com.logistock.inventory.infra.adapters.driven.jpa.repository.SpringDataInventoryMovementRepository;
import com.logistock.inventory.infra.adapters.driven.jpa.repository.SpringDataProductRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InventoryMovementRepositoryAdapter implements InventoryMovementRepositoryPort {

    private final SpringDataInventoryMovementRepository movementRepository;
    private final SpringDataProductRepository productRepository;
    private final InventoryMovementEntityMapper mapper;

    @Override
    public InventoryMovement save(InventoryMovement movement) {
        ProductEntity product = productRepository.getReferenceById(movement.productId());
        InventoryMovementEntity savedMovement = movementRepository.save(
                mapper.toEntity(movement, product)
        );
        return mapper.toDomain(savedMovement);
    }
}
