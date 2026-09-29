package com.logistock.inventory.domain.model;

import com.logistock.inventory.domain.enums.ProductCategory;
import com.logistock.inventory.domain.exception.InvalidProductException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    private static final Instant NOW = Instant.parse("2026-09-28T20:00:00Z");

    @Test
    void createsValidProduct() {
        Product product = Product.create(
                "  Barcode scanner  ",
                "  Warehouse device  ",
                ProductCategory.ELECTRONICS,
                12,
                new BigDecimal("245.90"),
                NOW
        );

        assertThat(product.id()).isNull();
        assertThat(product.name()).isEqualTo("Barcode scanner");
        assertThat(product.description()).isEqualTo("Warehouse device");
        assertThat(product.category()).isEqualTo(ProductCategory.ELECTRONICS);
        assertThat(product.stock()).isEqualTo(12);
        assertThat(product.price()).isEqualByComparingTo("245.90");
        assertThat(product.createdAt()).isEqualTo(NOW);
        assertThat(product.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void rejectsNegativeStock() {
        assertThatThrownBy(() -> Product.create(
                "Barcode scanner",
                null,
                ProductCategory.ELECTRONICS,
                -1,
                new BigDecimal("245.90"),
                NOW
        ))
                .isInstanceOf(InvalidProductException.class)
                .hasMessage("Product stock cannot be negative")
                .extracting("code")
                .isEqualTo("INVALID_PRODUCT_STOCK");
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> Product.create(
                " ",
                null,
                ProductCategory.OTHER,
                0,
                BigDecimal.ONE,
                NOW
        ))
                .isInstanceOf(InvalidProductException.class)
                .hasMessage("Product name is required");
    }

    @Test
    void rejectsNonPositivePrice() {
        assertThatThrownBy(() -> Product.create(
                "Box",
                null,
                ProductCategory.OTHER,
                0,
                BigDecimal.ZERO,
                NOW
        ))
                .isInstanceOf(InvalidProductException.class)
                .hasMessage("Product price must be greater than zero");
    }
}
