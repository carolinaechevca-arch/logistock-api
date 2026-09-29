package com.logistock.inventory.domain.port.out;

import com.logistock.inventory.domain.model.Product;

public interface ProductRepositoryPort {

    Product save(Product product);
}
