package com.logistock.inventory.domain.constants;

public final class InventoryConstants {

    public static final int OBSERVATION_MAX_LENGTH = 500;
    public static final int MOVEMENT_TYPE_MAX_LENGTH = 20;
    public static final int MINIMUM_MOVEMENT_QUANTITY = 1;

    public static final String INVALID_INVENTORY_MOVEMENT = "INVALID_INVENTORY_MOVEMENT";
    public static final String INSUFFICIENT_STOCK = "INSUFFICIENT_STOCK";
    public static final String CONCURRENT_INVENTORY_UPDATE = "CONCURRENT_INVENTORY_UPDATE";
    public static final String INVALID_MOVEMENT_QUANTITY_MESSAGE =
            "Inventory movement quantity must be greater than zero";
    public static final String INSUFFICIENT_STOCK_MESSAGE =
            "Insufficient stock for product %d. Available: %d, requested: %d";
    public static final String CONCURRENT_INVENTORY_UPDATE_MESSAGE =
            "Inventory was modified concurrently. Retry the operation";

    private InventoryConstants() {
    }
}
