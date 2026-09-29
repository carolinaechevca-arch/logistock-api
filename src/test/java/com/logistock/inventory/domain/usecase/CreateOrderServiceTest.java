package com.logistock.inventory.domain.usecase;

import com.logistock.inventory.domain.enums.OrderStatus;
import com.logistock.inventory.domain.enums.ProductCategory;
import com.logistock.inventory.domain.exception.InsufficientStockException;
import com.logistock.inventory.domain.exception.InvalidOrderException;
import com.logistock.inventory.domain.exception.ProductNotFoundException;
import com.logistock.inventory.domain.model.Order;
import com.logistock.inventory.domain.model.Product;
import com.logistock.inventory.domain.port.in.CreateOrderCommand;
import com.logistock.inventory.domain.port.in.CreateOrderItemCommand;
import com.logistock.inventory.domain.port.out.OrderRepositoryPort;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {

    private static final long FIRST_PRODUCT_ID = 10L;
    private static final long SECOND_PRODUCT_ID = 20L;
    private static final Instant NOW = Instant.parse("2026-09-28T20:00:00Z");

    @Mock
    private ProductRepositoryPort productRepository;

    @Mock
    private OrderRepositoryPort orderRepository;

    @Test
    void createsOrderWithoutChangingProductStock() {
        when(productRepository.findById(FIRST_PRODUCT_ID))
                .thenReturn(Optional.of(product(FIRST_PRODUCT_ID, 8)));
        when(productRepository.findById(SECOND_PRODUCT_ID))
                .thenReturn(Optional.of(product(SECOND_PRODUCT_ID, 5)));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        CreateOrderCommand command = command(
                new CreateOrderItemCommand(FIRST_PRODUCT_ID, 3),
                new CreateOrderItemCommand(SECOND_PRODUCT_ID, 2)
        );

        Order result = service().create(command);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        verify(productRepository, never()).save(any(Product.class));
        assertThat(captor.getValue().status()).isEqualTo(OrderStatus.CREATED);
        assertThat(captor.getValue().createdAt()).isEqualTo(NOW);
        assertThat(captor.getValue().items()).hasSize(2);
        assertThat(result.status()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void rejectsOrderWithoutItems() {
        assertThatThrownBy(() -> service().create(new CreateOrderCommand(List.of())))
                .isInstanceOf(InvalidOrderException.class);
        verifyNoInteractions(productRepository, orderRepository);
    }

    @Test
    void rejectsDuplicateProduct() {
        CreateOrderCommand command = command(
                new CreateOrderItemCommand(FIRST_PRODUCT_ID, 1),
                new CreateOrderItemCommand(FIRST_PRODUCT_ID, 2)
        );

        assertThatThrownBy(() -> service().create(command))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessage("Product 10 appears more than once in the order");
        verifyNoInteractions(productRepository, orderRepository);
    }

    @Test
    void rejectsMissingProduct() {
        when(productRepository.findById(FIRST_PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().create(command(
                new CreateOrderItemCommand(FIRST_PRODUCT_ID, 1)
        )))
                .isInstanceOf(ProductNotFoundException.class);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void rejectsQuantityGreaterThanAvailableStock() {
        when(productRepository.findById(FIRST_PRODUCT_ID))
                .thenReturn(Optional.of(product(FIRST_PRODUCT_ID, 2)));

        assertThatThrownBy(() -> service().create(command(
                new CreateOrderItemCommand(FIRST_PRODUCT_ID, 3)
        )))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessage("Insufficient stock for product 10. Available: 2, requested: 3");
        verifyNoInteractions(orderRepository);
    }

    @Test
    void rejectsInvalidProductId() {
        assertThatThrownBy(() -> service().create(command(
                new CreateOrderItemCommand(null, 1)
        )))
                .isInstanceOf(InvalidOrderException.class);
        verifyNoInteractions(productRepository, orderRepository);
    }

    private CreateOrderService service() {
        return new CreateOrderService(
                productRepository,
                orderRepository,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    private CreateOrderCommand command(CreateOrderItemCommand... items) {
        return new CreateOrderCommand(List.of(items));
    }

    private Product product(Long id, int stock) {
        return new Product(
                id,
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
