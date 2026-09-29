package com.logistock.inventory.domain.port.in;

import com.logistock.inventory.domain.model.Product;

public interface CreateProductUseCase {

    Product create(CreateProductCommand command);
}
