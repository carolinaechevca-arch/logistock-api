package com.logistock.inventory.domain.port.out;

import com.logistock.inventory.domain.model.Product;

import java.util.Optional;

public interface ProductRepositoryPort {

    Product save(Product product);

    Optional<Product> findById(Long id);
}
