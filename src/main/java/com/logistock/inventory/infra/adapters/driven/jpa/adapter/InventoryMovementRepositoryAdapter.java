package com.logistock.inventory.infra.adapters.driven.jpa.adapter;

import com.logistock.inventory.domain.model.InventoryMovement;
import com.logistock.inventory.domain.model.PageResult;
import com.logistock.inventory.domain.model.PaginationCriteria;
import com.logistock.inventory.domain.port.out.InventoryMovementRepositoryPort;
import com.logistock.inventory.infra.adapters.driven.jpa.constants.PersistenceConstants;
import com.logistock.inventory.infra.adapters.driven.jpa.entity.InventoryMovementEntity;
import com.logistock.inventory.infra.adapters.driven.jpa.entity.ProductEntity;
import com.logistock.inventory.infra.adapters.driven.jpa.mapper.InventoryMovementEntityMapper;
import com.logistock.inventory.infra.adapters.driven.jpa.repository.SpringDataInventoryMovementRepository;
import com.logistock.inventory.infra.adapters.driven.jpa.repository.SpringDataProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

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

    @Override
    public PageResult<InventoryMovement> findAll(PaginationCriteria criteria) {
        Sort sort = Sort.by(
                Sort.Order.desc(PersistenceConstants.CREATED_AT_ATTRIBUTE),
                Sort.Order.desc(PersistenceConstants.ID_ATTRIBUTE)
        );
        Page<InventoryMovementEntity> result = movementRepository.findAll(
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
