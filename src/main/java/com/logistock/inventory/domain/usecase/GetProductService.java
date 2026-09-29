package com.logistock.inventory.domain.usecase;

import com.logistock.inventory.domain.exception.InvalidProductIdException;
import com.logistock.inventory.domain.exception.ProductNotFoundException;
import com.logistock.inventory.domain.model.Product;
import com.logistock.inventory.domain.port.in.GetProductUseCase;
import com.logistock.inventory.domain.port.out.ProductRepositoryPort;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetProductService implements GetProductUseCase {

    private final ProductRepositoryPort productRepository;

    @Override
    public Product getById(Long id) {
        if (id == null || id <= 0) {
            throw new InvalidProductIdException();
        }
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
