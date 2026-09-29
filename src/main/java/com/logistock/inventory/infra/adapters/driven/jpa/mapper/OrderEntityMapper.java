package com.logistock.inventory.infra.adapters.driven.jpa.mapper;

import com.logistock.inventory.domain.model.Order;
import com.logistock.inventory.domain.model.OrderItem;
import com.logistock.inventory.infra.adapters.driven.jpa.entity.OrderEntity;
import com.logistock.inventory.infra.adapters.driven.jpa.entity.OrderItemEntity;
import com.logistock.inventory.infra.adapters.driven.jpa.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrderEntityMapper {

    Order toDomain(OrderEntity entity);

    @Mapping(target = "productId", source = "product.id")
    OrderItem toDomain(OrderItemEntity entity);

    @Mapping(target = "id", source = "order.id")
    @Mapping(target = "createdAt", source = "order.createdAt")
    @Mapping(target = "status", source = "order.status")
    @Mapping(target = "items", source = "items")
    OrderEntity toEntity(Order order, List<OrderItemEntity> items);

    @Mapping(target = "id", source = "item.id")
    @Mapping(target = "product", source = "product")
    @Mapping(target = "quantity", source = "item.quantity")
    OrderItemEntity toItemEntity(OrderItem item, ProductEntity product);
}
