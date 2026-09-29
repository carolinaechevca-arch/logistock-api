package com.logistock.inventory.infra.adapters.driven.jpa.adapter;

import com.logistock.inventory.domain.model.Product;
import com.logistock.inventory.domain.port.out.ProductRepositoryPort;
import com.logistock.inventory.infra.adapters.driven.jpa.entity.ProductEntity;
import com.logistock.inventory.infra.adapters.driven.jpa.mapper.ProductEntityMapper;
import com.logistock.inventory.infra.adapters.driven.jpa.repository.SpringDataProductRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final SpringDataProductRepository repository;
    private final ProductEntityMapper mapper;

    @Override
    public Product save(Product product) {
        ProductEntity savedProduct = repository.save(mapper.toEntity(product));
        return mapper.toDomain(savedProduct);
    }
}
