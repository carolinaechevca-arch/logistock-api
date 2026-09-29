package com.logistock.inventory.domain.port.out;

import com.logistock.inventory.domain.model.PageResult;
import com.logistock.inventory.domain.model.Product;
import com.logistock.inventory.domain.model.ProductSearchCriteria;

import java.util.Optional;

public interface ProductRepositoryPort {

    Product save(Product product);

    Optional<Product> findById(Long id);

    PageResult<Product> findAll(ProductSearchCriteria criteria);

    void delete(Product product);
}
