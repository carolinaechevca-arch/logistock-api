package com.logistock.inventory.domain.usecase;

import com.logistock.inventory.domain.enums.ProductCategory;
import com.logistock.inventory.domain.exception.InvalidPaginationException;
import com.logistock.inventory.domain.exception.InvalidProductIdException;
import com.logistock.inventory.domain.exception.ProductNotFoundException;
import com.logistock.inventory.domain.model.InventoryMovement;
import com.logistock.inventory.domain.model.PageResult;
import com.logistock.inventory.domain.model.PaginationCriteria;
import com.logistock.inventory.domain.model.Product;
import com.logistock.inventory.domain.port.out.InventoryMovementRepositoryPort;
import com.logistock.inventory.domain.port.out.ProductRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListProductInventoryMovementsServiceTest {

    private static final long PRODUCT_ID = 10L;
    private static final Instant NOW = Instant.parse("2026-09-28T20:00:00Z");

    @Mock
    private ProductRepositoryPort productRepository;

    @Mock
    private InventoryMovementRepositoryPort movementRepository;

    @Test
    void listsMovementsForExistingProduct() {
        PaginationCriteria criteria = new PaginationCriteria(0, 10);
        PageResult<InventoryMovement> expected = new PageResult<>(List.of(), 0, 10, 0, 0, true);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product()));
        when(movementRepository.findByProductId(PRODUCT_ID, criteria)).thenReturn(expected);
        ListProductInventoryMovementsService service = service();

        PageResult<InventoryMovement> result = service.list(PRODUCT_ID, criteria);

        assertThat(result).isEqualTo(expected);
        verify(movementRepository).findByProductId(PRODUCT_ID, criteria);
    }

    @Test
    void rejectsInvalidProductId() {
        ListProductInventoryMovementsService service = service();

        assertThatThrownBy(() -> service.list(0L, new PaginationCriteria(0, 10)))
                .isInstanceOf(InvalidProductIdException.class);
        verifyNoInteractions(productRepository, movementRepository);
    }

    @Test
    void rejectsInvalidPagination() {
        ListProductInventoryMovementsService service = service();

        assertThatThrownBy(() -> service.list(PRODUCT_ID, new PaginationCriteria(-1, 10)))
                .isInstanceOf(InvalidPaginationException.class);
        verifyNoInteractions(productRepository, movementRepository);
    }

    @Test
    void rejectsMissingProduct() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());
        ListProductInventoryMovementsService service = service();

        assertThatThrownBy(() -> service.list(PRODUCT_ID, new PaginationCriteria(0, 10)))
                .isInstanceOf(ProductNotFoundException.class);
        verifyNoInteractions(movementRepository);
    }

    private ListProductInventoryMovementsService service() {
        return new ListProductInventoryMovementsService(
                productRepository,
                movementRepository
        );
    }

    private Product product() {
        return new Product(
                PRODUCT_ID,
                "Barcode scanner",
                null,
                ProductCategory.ELECTRONICS,
                10,
                BigDecimal.TEN,
                NOW,
                NOW,
                0L
        );
    }
}
