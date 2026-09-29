package com.logistock.inventory.infra.adapters.driven.jpa.entity;

import com.logistock.inventory.domain.constants.ProductConstants;
import com.logistock.inventory.domain.enums.ProductCategory;
import com.logistock.inventory.infra.adapters.driven.jpa.constants.PersistenceConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = PersistenceConstants.PRODUCTS_TABLE,
        schema = PersistenceConstants.INVENTORY_SCHEMA
)
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = ProductConstants.NAME_MAX_LENGTH)
    private String name;

    @Column(length = ProductConstants.DESCRIPTION_MAX_LENGTH)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = ProductConstants.CATEGORY_MAX_LENGTH)
    private ProductCategory category;

    @Column(nullable = false)
    private int stock;

    @Column(
            nullable = false,
            precision = ProductConstants.PRICE_PRECISION,
            scale = ProductConstants.PRICE_SCALE
    )
    private BigDecimal price;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

}
