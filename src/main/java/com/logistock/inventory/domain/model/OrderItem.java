package com.logistock.inventory.domain.model;

import com.logistock.inventory.domain.constants.OrderConstants;
import com.logistock.inventory.domain.exception.InvalidOrderException;

public record OrderItem(
        Long id,
        Long productId,
        int quantity
) {

    public OrderItem {
        if (productId == null || productId <= 0) {
            throw new InvalidOrderException(OrderConstants.INVALID_PRODUCT_ID_MESSAGE);
        }
        if (quantity < OrderConstants.MINIMUM_ITEM_QUANTITY) {
            throw new InvalidOrderException(OrderConstants.INVALID_ITEM_QUANTITY_MESSAGE);
        }
    }

    public static OrderItem create(Long productId, int quantity) {
        return new OrderItem(null, productId, quantity);
    }
}
