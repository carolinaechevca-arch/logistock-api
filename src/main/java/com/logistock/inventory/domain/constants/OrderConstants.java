package com.logistock.inventory.domain.constants;

public final class OrderConstants {

    public static final int STATUS_MAX_LENGTH = 20;
    public static final int MINIMUM_ITEM_QUANTITY = 1;

    public static final String INVALID_ORDER = "INVALID_ORDER";
    public static final String EMPTY_ORDER_MESSAGE = "An order must contain at least one item";
    public static final String INVALID_ITEM_QUANTITY_MESSAGE =
            "Order item quantity must be greater than zero";
    public static final String DUPLICATE_PRODUCT_MESSAGE =
            "Product %d appears more than once in the order";
    public static final String NULL_ITEM_MESSAGE = "Order items cannot be null";
    public static final String INVALID_PRODUCT_ID_MESSAGE =
            "A valid product id is required for every order item";
    public static final String CREATION_DATE_REQUIRED_MESSAGE = "Order creation date is required";
    public static final String STATUS_REQUIRED_MESSAGE = "Order status is required";

    private OrderConstants() {
    }
}
