package com.logistock.inventory.domain.usecase;

import com.logistock.inventory.domain.model.Product;
import com.logistock.inventory.domain.port.in.CreateProductCommand;
import com.logistock.inventory.domain.port.in.CreateProductUseCase;
import com.logistock.inventory.domain.port.out.ProductRepositoryPort;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class CreateProductService implements CreateProductUseCase {

    private final ProductRepositoryPort productRepository;
    private final Clock clock;

    public CreateProductService(ProductRepositoryPort productRepository, Clock clock) {
        this.productRepository = Objects.requireNonNull(productRepository);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public Product create(CreateProductCommand command) {
        Objects.requireNonNull(command);
        Instant now = clock.instant();
        Product product = Product.create(
                command.name(),
                command.description(),
                command.category(),
                command.stock(),
                command.price(),
                now
        );
        return productRepository.save(product);
    }
}
