package com.logistock.inventory.domain.usecase;

import com.logistock.inventory.domain.enums.ProductCategory;
import com.logistock.inventory.domain.model.Product;
import com.logistock.inventory.domain.port.in.CreateProductCommand;
import com.logistock.inventory.domain.port.out.ProductRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateProductServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-28T20:00:00Z");

    @Mock
    private ProductRepositoryPort productRepository;

    @Test
    void createsAndPersistsProduct() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        CreateProductService service = new CreateProductService(productRepository, clock);
        CreateProductCommand command = new CreateProductCommand(
                "Barcode scanner",
                "Warehouse device",
                ProductCategory.ELECTRONICS,
                12,
                new BigDecimal("245.90")
        );
        Product persistedProduct = new Product(
                1L,
                command.name(),
                command.description(),
                command.category(),
                command.stock(),
                command.price(),
                NOW,
                NOW,
                0L
        );
        when(productRepository.save(org.mockito.ArgumentMatchers.any(Product.class)))
                .thenReturn(persistedProduct);

        Product result = service.create(command);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product productToPersist = captor.getValue();
        assertThat(productToPersist.id()).isNull();
        assertThat(productToPersist.createdAt()).isEqualTo(NOW);
        assertThat(productToPersist.updatedAt()).isEqualTo(NOW);
        assertThat(result).isEqualTo(persistedProduct);
    }
}
