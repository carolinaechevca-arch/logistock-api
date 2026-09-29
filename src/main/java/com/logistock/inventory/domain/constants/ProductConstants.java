package com.logistock.inventory.domain.constants;

public final class ProductConstants {

    public static final int NAME_MAX_LENGTH = 120;
    public static final int DESCRIPTION_MAX_LENGTH = 500;
    public static final int CATEGORY_MAX_LENGTH = 30;
    public static final int PRICE_PRECISION = 15;
    public static final int PRICE_SCALE = 2;
    public static final int MINIMUM_STOCK = 0;
    public static final int RESTOCK_THRESHOLD = 5;
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    public static final String PRODUCT_NAME_REQUIRED = "PRODUCT_NAME_REQUIRED";
    public static final String PRODUCT_CATEGORY_REQUIRED = "PRODUCT_CATEGORY_REQUIRED";
    public static final String INVALID_PRODUCT_STOCK = "INVALID_PRODUCT_STOCK";
    public static final String INVALID_PRODUCT_PRICE = "INVALID_PRODUCT_PRICE";
    public static final String PRODUCT_TIMESTAMPS_REQUIRED = "PRODUCT_TIMESTAMPS_REQUIRED";
    public static final String INVALID_PRODUCT_ID = "INVALID_PRODUCT_ID";
    public static final String PRODUCT_NOT_FOUND = "PRODUCT_NOT_FOUND";
    public static final String INVALID_STOCK_RANGE = "INVALID_STOCK_RANGE";
    public static final String INVALID_PAGINATION = "INVALID_PAGINATION";

    public static final String PRODUCT_NAME_REQUIRED_MESSAGE = "Product name is required";
    public static final String PRODUCT_CATEGORY_REQUIRED_MESSAGE = "Product category is required";
    public static final String INVALID_PRODUCT_STOCK_MESSAGE = "Product stock cannot be negative";
    public static final String INVALID_PRODUCT_PRICE_MESSAGE = "Product price must be greater than zero";
    public static final String PRODUCT_TIMESTAMPS_REQUIRED_MESSAGE = "Product timestamps are required";
    public static final String INVALID_PRODUCT_ID_MESSAGE = "Product id must be greater than zero";
    public static final String PRODUCT_NOT_FOUND_MESSAGE = "Product not found with id %d";
    public static final String INVALID_STOCK_RANGE_MESSAGE =
            "minStock must be less than or equal to maxStock";
    public static final String NEGATIVE_STOCK_FILTER_MESSAGE = "Stock filters cannot be negative";
    public static final String INVALID_PAGINATION_MESSAGE =
            "Page must be zero or greater and size must be between 1 and 100";

    private ProductConstants() {
    }
}
