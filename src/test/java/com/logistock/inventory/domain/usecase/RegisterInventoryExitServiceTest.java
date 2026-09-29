package com.logistock.inventory.domain.usecase;

import com.logistock.inventory.domain.enums.InventoryMovementType;
import com.logistock.inventory.domain.enums.ProductCategory;
import com.logistock.inventory.domain.exception.InsufficientStockException;
import com.logistock.inventory.domain.exception.InvalidInventoryMovementException;
import com.logistock.inventory.domain.exception.ProductNotFoundException;
import com.logistock.inventory.domain.model.InventoryMovement;
import com.logistock.inventory.domain.model.Product;
import com.logistock.inventory.domain.port.in.RegisterInventoryExitCommand;
import com.logistock.inventory.domain.port.out.InventoryMovementRepositoryPort;
import com.logistock.inventory.domain.port.out.ProductRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterInventoryExitServiceTest {

    private static final long PRODUCT_ID = 10L;
    private static final Instant NOW = Instant.parse("2026-09-28T20:00:00Z");

    @Mock
    private ProductRepositoryPort productRepository;

    @Mock
    private InventoryMovementRepositoryPort movementRepository;

    @Test
    void registersExitAndDecreasesStock() {
        Product product = product(10);
        RegisterInventoryExitCommand command = new RegisterInventoryExitCommand(
                PRODUCT_ID,
                4,
                "Customer shipment"
        );
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movementRepository.save(any(InventoryMovement.class))).thenAnswer(invocation -> {
            InventoryMovement movement = invocation.getArgument(0);
            return new InventoryMovement(
                    1L,
                    movement.productId(),
                    movement.type(),
                    movement.quantity(),
                    movement.createdAt(),
                    movement.observation()
            );
        });
        RegisterInventoryExitService service = service();

        InventoryMovement result = service.register(command);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().stock()).isEqualTo(6);
        assertThat(productCaptor.getValue().updatedAt()).isEqualTo(NOW);
        assertThat(result.type()).isEqualTo(InventoryMovementType.EXIT);
        assertThat(result.quantity()).isEqualTo(4);
    }

    @Test
    void rejectsExitGreaterThanAvailableStock() {
        Product product = product(3);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        RegisterInventoryExitService service = service();
        RegisterInventoryExitCommand command = new RegisterInventoryExitCommand(
                PRODUCT_ID,
                4,
                null
        );

        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessage("Insufficient stock for product 10. Available: 3, requested: 4");
        verifyNoInteractions(movementRepository);
    }

    @Test
    void rejectsNonPositiveQuantity() {
        RegisterInventoryExitService service = service();
        RegisterInventoryExitCommand command = new RegisterInventoryExitCommand(
                PRODUCT_ID,
                0,
                null
        );

        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(InvalidInventoryMovementException.class);
        verifyNoInteractions(productRepository, movementRepository);
    }

    @Test
    void rejectsMissingProduct() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());
        RegisterInventoryExitService service = service();
        RegisterInventoryExitCommand command = new RegisterInventoryExitCommand(
                PRODUCT_ID,
                4,
                null
        );

        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(ProductNotFoundException.class);
        verifyNoInteractions(movementRepository);
    }

    private RegisterInventoryExitService service() {
        return new RegisterInventoryExitService(
                productRepository,
                movementRepository,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    private Product product(int stock) {
        return new Product(
                PRODUCT_ID,
                "Barcode scanner",
                "Warehouse device",
                ProductCategory.ELECTRONICS,
                stock,
                new BigDecimal("245.90"),
                NOW.minusSeconds(3600),
                NOW.minusSeconds(3600),
                0L
        );
    }
}
