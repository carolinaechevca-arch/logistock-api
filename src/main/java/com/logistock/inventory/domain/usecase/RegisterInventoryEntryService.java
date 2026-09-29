package com.logistock.inventory.domain.usecase;

import com.logistock.inventory.domain.constants.InventoryConstants;
import com.logistock.inventory.domain.exception.InvalidInventoryMovementException;
import com.logistock.inventory.domain.exception.InvalidProductIdException;
import com.logistock.inventory.domain.exception.ProductNotFoundException;
import com.logistock.inventory.domain.model.InventoryMovement;
import com.logistock.inventory.domain.model.Product;
import com.logistock.inventory.domain.port.in.RegisterInventoryEntryCommand;
import com.logistock.inventory.domain.port.in.RegisterInventoryEntryUseCase;
import com.logistock.inventory.domain.port.out.InventoryMovementRepositoryPort;
import com.logistock.inventory.domain.port.out.ProductRepositoryPort;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

@RequiredArgsConstructor
public class RegisterInventoryEntryService implements RegisterInventoryEntryUseCase {

    @NonNull
    private final ProductRepositoryPort productRepository;

    @NonNull
    private final InventoryMovementRepositoryPort movementRepository;

    @NonNull
    private final Clock clock;

    @Override
    public InventoryMovement register(RegisterInventoryEntryCommand command) {
        Objects.requireNonNull(command);
        validateCommand(command);
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));
        Instant now = clock.instant();
        productRepository.save(product.increaseStock(command.quantity(), now));
        InventoryMovement movement = InventoryMovement.entry(
                product.id(),
                command.quantity(),
                now,
                command.observation()
        );
        return movementRepository.save(movement);
    }

    private void validateCommand(RegisterInventoryEntryCommand command) {
        if (command.productId() == null || command.productId() <= 0) {
            throw new InvalidProductIdException();
        }
        if (command.quantity() < InventoryConstants.MINIMUM_MOVEMENT_QUANTITY) {
            throw new InvalidInventoryMovementException(
                    InventoryConstants.INVALID_MOVEMENT_QUANTITY_MESSAGE
            );
        }
    }
}
