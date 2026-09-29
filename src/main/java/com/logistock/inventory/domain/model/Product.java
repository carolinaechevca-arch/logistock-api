package com.logistock.inventory.domain.model;

import com.logistock.inventory.domain.constants.ProductConstants;
import com.logistock.inventory.domain.enums.ProductCategory;
import com.logistock.inventory.domain.exception.InvalidProductException;
import com.logistock.inventory.domain.constants.InventoryConstants;
import com.logistock.inventory.domain.exception.InvalidInventoryMovementException;
import com.logistock.inventory.domain.exception.InsufficientStockException;

import java.math.BigDecimal;
import java.time.Instant;

public record Product(
        Long id,
        String name,
        String description,
        ProductCategory category,
        int stock,
        BigDecimal price,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {

    public Product {
        name = normalizeRequiredName(name);
        description = normalizeDescription(description);
        validateCategory(category);
        validateStock(stock);
        validatePrice(price);
        validateTimestamps(createdAt, updatedAt);
    }

    public static Product create(
            String name,
            String description,
            ProductCategory category,
            int stock,
            BigDecimal price,
            Instant now
    ) {
        return new Product(null, name, description, category, stock, price, now, now, null);
    }

    public Product increaseStock(int quantity, Instant now) {
        if (quantity < InventoryConstants.MINIMUM_MOVEMENT_QUANTITY) {
            throw new InvalidInventoryMovementException(
                    InventoryConstants.INVALID_MOVEMENT_QUANTITY_MESSAGE
            );
        }
        return new Product(
                id,
                name,
                description,
                category,
                Math.addExact(stock, quantity),
                price,
                createdAt,
                now,
                version
        );
    }

    public Product decreaseStock(int quantity, Instant now) {
        if (quantity < InventoryConstants.MINIMUM_MOVEMENT_QUANTITY) {
            throw new InvalidInventoryMovementException(
                    InventoryConstants.INVALID_MOVEMENT_QUANTITY_MESSAGE
            );
        }
        if (quantity > stock) {
            throw new InsufficientStockException(id, stock, quantity);
        }
        return new Product(
                id,
                name,
                description,
                category,
                stock - quantity,
                price,
                createdAt,
                now,
                version
        );
    }

    private static String normalizeRequiredName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidProductException(
                    ProductConstants.PRODUCT_NAME_REQUIRED,
                    ProductConstants.PRODUCT_NAME_REQUIRED_MESSAGE
            );
        }
        return name.trim();
    }

    private static String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }

    private static void validateCategory(ProductCategory category) {
        if (category == null) {
            throw new InvalidProductException(
                    ProductConstants.PRODUCT_CATEGORY_REQUIRED,
                    ProductConstants.PRODUCT_CATEGORY_REQUIRED_MESSAGE
            );
        }
    }

    private static void validateStock(int stock) {
        if (stock < ProductConstants.MINIMUM_STOCK) {
            throw new InvalidProductException(
                    ProductConstants.INVALID_PRODUCT_STOCK,
                    ProductConstants.INVALID_PRODUCT_STOCK_MESSAGE
            );
        }
    }

    private static void validatePrice(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new InvalidProductException(
                    ProductConstants.INVALID_PRODUCT_PRICE,
                    ProductConstants.INVALID_PRODUCT_PRICE_MESSAGE
            );
        }
    }

    private static void validateTimestamps(Instant createdAt, Instant updatedAt) {
        if (createdAt == null || updatedAt == null) {
            throw new InvalidProductException(
                    ProductConstants.PRODUCT_TIMESTAMPS_REQUIRED,
                    ProductConstants.PRODUCT_TIMESTAMPS_REQUIRED_MESSAGE
            );
        }
    }
}
