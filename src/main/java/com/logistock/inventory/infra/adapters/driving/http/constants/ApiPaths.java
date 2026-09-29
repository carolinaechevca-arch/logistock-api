package com.logistock.inventory.infra.adapters.driving.http.constants;

public final class ApiPaths {

    public static final String PRODUCTS = "/api/v1/products";
    public static final String PRODUCT_BY_ID = "/{id}";
    public static final String PRODUCTS_RESTOCK = "/restock";
    public static final String INVENTORY = "/api/v1/inventory";
    public static final String INVENTORY_ENTRIES = "/entries";
    public static final String INVENTORY_EXITS = "/exits";
    public static final String INVENTORY_MOVEMENTS = "/movements";

    private ApiPaths() {
    }
}
