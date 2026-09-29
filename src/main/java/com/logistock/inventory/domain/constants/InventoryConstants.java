package com.logistock.inventory.domain.constants;

public final class InventoryConstants {

    public static final int OBSERVATION_MAX_LENGTH = 500;
    public static final int MOVEMENT_TYPE_MAX_LENGTH = 20;
    public static final int MINIMUM_MOVEMENT_QUANTITY = 1;

    public static final String INVALID_INVENTORY_MOVEMENT = "INVALID_INVENTORY_MOVEMENT";
    public static final String INVALID_MOVEMENT_QUANTITY_MESSAGE =
            "Inventory movement quantity must be greater than zero";

    private InventoryConstants() {
    }
}
