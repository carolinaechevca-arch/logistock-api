package com.logistock.inventory.domain.usecase;

import com.logistock.inventory.domain.exception.InvalidProductIdException;
import com.logistock.inventory.domain.exception.ProductHasStockException;
import com.logistock.inventory.domain.exception.ProductNotFoundException;
import com.logistock.inventory.domain.model.Product;
import com.logistock.inventory.domain.port.in.DeleteProductUseCase;
import com.logistock.inventory.domain.port.out.ProductRepositoryPort;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteProductService implements DeleteProductUseCase {

    @NonNull
    private final ProductRepositoryPort productRepository;

    @Override
    public void deleteById(Long id) {
        if (id == null || id <= 0) {
            throw new InvalidProductIdException();
        }
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        if (product.stock() > 0) {
            throw new ProductHasStockException(product.id(), product.stock());
        }
        productRepository.delete(product);
    }
}
