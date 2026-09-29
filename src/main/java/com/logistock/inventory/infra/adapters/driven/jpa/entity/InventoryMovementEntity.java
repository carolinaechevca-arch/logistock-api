package com.logistock.inventory.infra.adapters.driven.jpa.entity;

import com.logistock.inventory.domain.constants.InventoryConstants;
import com.logistock.inventory.domain.enums.InventoryMovementType;
import com.logistock.inventory.infra.adapters.driven.jpa.constants.PersistenceConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = PersistenceConstants.INVENTORY_MOVEMENTS_TABLE,
        schema = PersistenceConstants.INVENTORY_SCHEMA
)
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryMovementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = InventoryConstants.MOVEMENT_TYPE_MAX_LENGTH)
    private InventoryMovementType type;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(length = InventoryConstants.OBSERVATION_MAX_LENGTH)
    private String observation;
}
