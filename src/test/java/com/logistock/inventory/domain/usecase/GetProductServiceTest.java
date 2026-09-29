package com.logistock.inventory.domain.usecase;

import com.logistock.inventory.domain.enums.ProductCategory;
import com.logistock.inventory.domain.exception.InvalidProductIdException;
import com.logistock.inventory.domain.exception.ProductNotFoundException;
import com.logistock.inventory.domain.model.Product;
import com.logistock.inventory.domain.port.out.ProductRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProductServiceTest {

    private static final long PRODUCT_ID = 10L;
    private static final Instant NOW = Instant.parse("2026-09-28T20:00:00Z");

    @Mock
    private ProductRepositoryPort productRepository;

    @Test
    void getsProductById() {
        Product product = product();
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        GetProductService service = new GetProductService(productRepository);

        Product result = service.getById(PRODUCT_ID);

        assertThat(result).isEqualTo(product);
        verify(productRepository).findById(PRODUCT_ID);
    }

    @Test
    void rejectsInvalidProductId() {
        GetProductService service = new GetProductService(productRepository);

        assertThatThrownBy(() -> service.getById(0L))
                .isInstanceOf(InvalidProductIdException.class)
                .hasMessage("Product id must be greater than zero");
        verifyNoInteractions(productRepository);
    }

    @Test
    void throwsExceptionWhenProductDoesNotExist() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());
        GetProductService service = new GetProductService(productRepository);

        assertThatThrownBy(() -> service.getById(PRODUCT_ID))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found with id 10");
        verify(productRepository).findById(PRODUCT_ID);
    }

    private Product product() {
        return new Product(
                PRODUCT_ID,
                "Barcode scanner",
                "Warehouse device",
                ProductCategory.ELECTRONICS,
                12,
                new BigDecimal("245.90"),
                NOW,
                NOW,
                0L
        );
    }
}
