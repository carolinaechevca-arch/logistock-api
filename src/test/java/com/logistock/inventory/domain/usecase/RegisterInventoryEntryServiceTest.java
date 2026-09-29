package com.logistock.inventory.domain.usecase;

import com.logistock.inventory.domain.enums.InventoryMovementType;
import com.logistock.inventory.domain.enums.ProductCategory;
import com.logistock.inventory.domain.exception.InvalidInventoryMovementException;
import com.logistock.inventory.domain.exception.ProductNotFoundException;
import com.logistock.inventory.domain.model.InventoryMovement;
import com.logistock.inventory.domain.model.Product;
import com.logistock.inventory.domain.port.in.RegisterInventoryEntryCommand;
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
class RegisterInventoryEntryServiceTest {

    private static final long PRODUCT_ID = 10L;
    private static final Instant NOW = Instant.parse("2026-09-28T20:00:00Z");

    @Mock
    private ProductRepositoryPort productRepository;

    @Mock
    private InventoryMovementRepositoryPort movementRepository;

    @Test
    void registersEntryAndIncreasesStock() {
        Product product = product(10);
        RegisterInventoryEntryCommand command = new RegisterInventoryEntryCommand(
                PRODUCT_ID,
                5,
                "Supplier delivery"
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
        RegisterInventoryEntryService service = new RegisterInventoryEntryService(
                productRepository,
                movementRepository,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );

        InventoryMovement result = service.register(command);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().stock()).isEqualTo(15);
        assertThat(productCaptor.getValue().updatedAt()).isEqualTo(NOW);
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.type()).isEqualTo(InventoryMovementType.ENTRY);
        assertThat(result.quantity()).isEqualTo(5);
        assertThat(result.createdAt()).isEqualTo(NOW);
    }

    @Test
    void rejectsNonPositiveQuantity() {
        RegisterInventoryEntryService service = service();
        RegisterInventoryEntryCommand command = new RegisterInventoryEntryCommand(
                PRODUCT_ID,
                0,
                null
        );

        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(InvalidInventoryMovementException.class)
                .hasMessage("Inventory movement quantity must be greater than zero");
        verifyNoInteractions(productRepository, movementRepository);
    }

    @Test
    void rejectsMissingProduct() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());
        RegisterInventoryEntryService service = service();
        RegisterInventoryEntryCommand command = new RegisterInventoryEntryCommand(
                PRODUCT_ID,
                5,
                null
        );

        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found with id 10");
        verifyNoInteractions(movementRepository);
    }

    private RegisterInventoryEntryService service() {
        return new RegisterInventoryEntryService(
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
