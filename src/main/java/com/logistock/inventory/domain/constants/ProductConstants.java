package com.logistock.inventory.domain.constants;

public final class ProductConstants {

    public static final int NAME_MAX_LENGTH = 120;
    public static final int DESCRIPTION_MAX_LENGTH = 500;
    public static final int CATEGORY_MAX_LENGTH = 30;
    public static final int PRICE_PRECISION = 15;
    public static final int PRICE_SCALE = 2;
    public static final int MINIMUM_STOCK = 0;
    public static final int RESTOCK_THRESHOLD = 5;

    public static final String PRODUCT_NAME_REQUIRED = "PRODUCT_NAME_REQUIRED";
    public static final String PRODUCT_CATEGORY_REQUIRED = "PRODUCT_CATEGORY_REQUIRED";
    public static final String INVALID_PRODUCT_STOCK = "INVALID_PRODUCT_STOCK";
    public static final String INVALID_PRODUCT_PRICE = "INVALID_PRODUCT_PRICE";
    public static final String PRODUCT_TIMESTAMPS_REQUIRED = "PRODUCT_TIMESTAMPS_REQUIRED";

    public static final String PRODUCT_NAME_REQUIRED_MESSAGE = "Product name is required";
    public static final String PRODUCT_CATEGORY_REQUIRED_MESSAGE = "Product category is required";
    public static final String INVALID_PRODUCT_STOCK_MESSAGE = "Product stock cannot be negative";
    public static final String INVALID_PRODUCT_PRICE_MESSAGE = "Product price must be greater than zero";
    public static final String PRODUCT_TIMESTAMPS_REQUIRED_MESSAGE = "Product timestamps are required";

    private ProductConstants() {
    }
}
