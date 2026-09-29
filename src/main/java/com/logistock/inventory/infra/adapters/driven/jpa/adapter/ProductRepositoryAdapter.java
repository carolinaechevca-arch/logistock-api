package com.logistock.inventory.infra.adapters.driven.jpa.adapter;

import com.logistock.inventory.domain.model.PageResult;
import com.logistock.inventory.domain.model.Product;
import com.logistock.inventory.domain.model.ProductSearchCriteria;
import com.logistock.inventory.domain.port.out.ProductRepositoryPort;
import com.logistock.inventory.infra.adapters.driven.jpa.constants.PersistenceConstants;
import com.logistock.inventory.infra.adapters.driven.jpa.entity.ProductEntity;
import com.logistock.inventory.infra.adapters.driven.jpa.mapper.ProductEntityMapper;
import com.logistock.inventory.infra.adapters.driven.jpa.repository.SpringDataProductRepository;
import com.logistock.inventory.infra.adapters.driven.jpa.repository.ProductSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Optional;

@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final SpringDataProductRepository repository;
    private final ProductEntityMapper mapper;

    @Override
    public Product save(Product product) {
        ProductEntity savedProduct = repository.save(mapper.toEntity(product));
        return mapper.toDomain(savedProduct);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public PageResult<Product> findAll(ProductSearchCriteria criteria) {
        PageRequest pageable = PageRequest.of(
                criteria.page(),
                criteria.size(),
                Sort.by(Sort.Direction.ASC, PersistenceConstants.ID_ATTRIBUTE)
        );
        Page<ProductEntity> result = repository.findAll(
                ProductSpecifications.from(criteria),
                pageable
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
