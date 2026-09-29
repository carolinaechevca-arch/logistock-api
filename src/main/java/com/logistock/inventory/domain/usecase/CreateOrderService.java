package com.logistock.inventory.domain.usecase;

import com.logistock.inventory.domain.constants.OrderConstants;
import com.logistock.inventory.domain.exception.InsufficientStockException;
import com.logistock.inventory.domain.exception.InvalidOrderException;
import com.logistock.inventory.domain.exception.ProductNotFoundException;
import com.logistock.inventory.domain.model.Order;
import com.logistock.inventory.domain.model.OrderItem;
import com.logistock.inventory.domain.model.Product;
import com.logistock.inventory.domain.port.in.CreateOrderCommand;
import com.logistock.inventory.domain.port.in.CreateOrderItemCommand;
import com.logistock.inventory.domain.port.in.CreateOrderUseCase;
import com.logistock.inventory.domain.port.out.OrderRepositoryPort;
import com.logistock.inventory.domain.port.out.ProductRepositoryPort;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor
public class CreateOrderService implements CreateOrderUseCase {

    @NonNull
    private final ProductRepositoryPort productRepository;

    @NonNull
    private final OrderRepositoryPort orderRepository;

    @NonNull
    private final Clock clock;

    @Override
    public Order create(CreateOrderCommand command) {
        Objects.requireNonNull(command);
        validateItems(command.items());
        List<OrderItem> items = command.items().stream()
                .map(this::validateAndCreateItem)
                .toList();
        return orderRepository.save(Order.create(items, clock.instant()));
    }

    private void validateItems(List<CreateOrderItemCommand> items) {
        if (items == null || items.isEmpty()) {
            throw new InvalidOrderException(OrderConstants.EMPTY_ORDER_MESSAGE);
        }
        Set<Long> productIds = new HashSet<>();
        for (CreateOrderItemCommand item : items) {
            if (item == null) {
                throw new InvalidOrderException(OrderConstants.NULL_ITEM_MESSAGE);
            }
            if (item.productId() == null || item.productId() <= 0) {
                throw new InvalidOrderException(OrderConstants.INVALID_PRODUCT_ID_MESSAGE);
            }
            if (!productIds.add(item.productId())) {
                throw new InvalidOrderException(
                        OrderConstants.DUPLICATE_PRODUCT_MESSAGE.formatted(item.productId())
                );
            }
        }
    }

    private OrderItem validateAndCreateItem(CreateOrderItemCommand item) {
        if (item.quantity() < OrderConstants.MINIMUM_ITEM_QUANTITY) {
            throw new InvalidOrderException(OrderConstants.INVALID_ITEM_QUANTITY_MESSAGE);
        }
        Product product = productRepository.findById(item.productId())
                .orElseThrow(() -> new ProductNotFoundException(item.productId()));
        if (item.quantity() > product.stock()) {
            throw new InsufficientStockException(
                    product.id(),
                    product.stock(),
                    item.quantity()
            );
        }
        return OrderItem.create(item.productId(), item.quantity());
    }
}
