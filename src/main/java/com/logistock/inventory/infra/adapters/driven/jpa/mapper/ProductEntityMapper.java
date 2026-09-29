package com.logistock.inventory.infra.adapters.driven.jpa.mapper;

import com.logistock.inventory.domain.model.Product;
import com.logistock.inventory.infra.adapters.driven.jpa.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProductEntityMapper {

    ProductEntity toEntity(Product product);

    Product toDomain(ProductEntity entity);
}
