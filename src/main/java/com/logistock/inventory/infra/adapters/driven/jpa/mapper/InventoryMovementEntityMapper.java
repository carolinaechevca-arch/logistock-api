package com.logistock.inventory.infra.adapters.driven.jpa.mapper;

import com.logistock.inventory.domain.model.InventoryMovement;
import com.logistock.inventory.infra.adapters.driven.jpa.entity.InventoryMovementEntity;
import com.logistock.inventory.infra.adapters.driven.jpa.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface InventoryMovementEntityMapper {

    @Mapping(target = "productId", source = "product.id")
    InventoryMovement toDomain(InventoryMovementEntity entity);

    @Mapping(target = "id", source = "movement.id")
    @Mapping(target = "product", source = "product")
    @Mapping(target = "type", source = "movement.type")
    @Mapping(target = "quantity", source = "movement.quantity")
    @Mapping(target = "createdAt", source = "movement.createdAt")
    @Mapping(target = "observation", source = "movement.observation")
    InventoryMovementEntity toEntity(InventoryMovement movement, ProductEntity product);
}
