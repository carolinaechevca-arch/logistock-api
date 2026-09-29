package com.logistock.inventory.domain.model;

import com.logistock.inventory.domain.enums.InventoryMovementType;
import com.logistock.inventory.domain.exception.InvalidInventoryMovementException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryMovementTest {

    private static final Instant NOW = Instant.parse("2026-09-28T20:00:00Z");

    @Test
    void createsInventoryEntry() {
        InventoryMovement movement = InventoryMovement.entry(
                10L,
                5,
                NOW,
                "  Supplier delivery  "
        );

        assertThat(movement.id()).isNull();
        assertThat(movement.productId()).isEqualTo(10L);
        assertThat(movement.type()).isEqualTo(InventoryMovementType.ENTRY);
        assertThat(movement.quantity()).isEqualTo(5);
        assertThat(movement.createdAt()).isEqualTo(NOW);
        assertThat(movement.observation()).isEqualTo("Supplier delivery");
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThatThrownBy(() -> InventoryMovement.entry(10L, 0, NOW, null))
                .isInstanceOf(InvalidInventoryMovementException.class)
                .hasMessage("Inventory movement quantity must be greater than zero");
    }

    @Test
    void createsInventoryExit() {
        InventoryMovement movement = InventoryMovement.exit(
                10L,
                3,
                NOW,
                "Customer shipment"
        );

        assertThat(movement.type()).isEqualTo(InventoryMovementType.EXIT);
        assertThat(movement.quantity()).isEqualTo(3);
        assertThat(movement.observation()).isEqualTo("Customer shipment");
    }
}
