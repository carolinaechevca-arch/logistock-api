package com.logistock.inventory.domain.usecase;

import com.logistock.inventory.domain.enums.ProductCategory;
import com.logistock.inventory.domain.exception.InvalidProductIdException;
import com.logistock.inventory.domain.exception.ProductHasStockException;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteProductServiceTest {

    private static final long PRODUCT_ID = 10L;
    private static final Instant NOW = Instant.parse("2026-09-28T20:00:00Z");

    @Mock
    private ProductRepositoryPort productRepository;

    @Test
    void deletesProductWithZeroStock() {
        Product product = product(0);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        DeleteProductService service = new DeleteProductService(productRepository);

        service.deleteById(PRODUCT_ID);

        verify(productRepository).findById(PRODUCT_ID);
        verify(productRepository).delete(product);
    }

    @Test
    void rejectsProductWithStock() {
        Product product = product(5);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        DeleteProductService service = new DeleteProductService(productRepository);

        assertThatThrownBy(() -> service.deleteById(PRODUCT_ID))
                .isInstanceOf(ProductHasStockException.class)
                .hasMessage("Product 10 cannot be deleted because it has 5 units in stock");
        verify(productRepository, never()).delete(product);
    }

    @Test
    void throwsExceptionWhenProductDoesNotExist() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());
        DeleteProductService service = new DeleteProductService(productRepository);

        assertThatThrownBy(() -> service.deleteById(PRODUCT_ID))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found with id 10");
    }

    @Test
    void rejectsInvalidProductId() {
        DeleteProductService service = new DeleteProductService(productRepository);

        assertThatThrownBy(() -> service.deleteById(0L))
                .isInstanceOf(InvalidProductIdException.class);
        verifyNoInteractions(productRepository);
    }

    private Product product(int stock) {
        return new Product(
                PRODUCT_ID,
                "Barcode scanner",
                "Warehouse device",
                ProductCategory.ELECTRONICS,
                stock,
                new BigDecimal("245.90"),
                NOW,
                NOW,
                0L
        );
    }
}
