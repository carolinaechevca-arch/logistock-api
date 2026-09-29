package com.logistock.inventory.domain.usecase;

import com.logistock.inventory.domain.exception.InvalidPaginationException;
import com.logistock.inventory.domain.model.PageResult;
import com.logistock.inventory.domain.model.PaginationCriteria;
import com.logistock.inventory.domain.model.Product;
import com.logistock.inventory.domain.model.ProductSearchCriteria;
import com.logistock.inventory.domain.port.out.ProductRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListRestockProductsServiceTest {

    @Mock
    private ProductRepositoryPort productRepository;

    @Test
    void listsProductsWithStockBelowFive() {
        PaginationCriteria pagination = new PaginationCriteria(0, 10);
        ProductSearchCriteria expectedCriteria = new ProductSearchCriteria(
                null,
                null,
                4,
                0,
                10
        );
        PageResult<Product> expected = new PageResult<>(List.of(), 0, 10, 0, 0, true);
        when(productRepository.findAll(expectedCriteria)).thenReturn(expected);
        ListRestockProductsService service = new ListRestockProductsService(productRepository);

        PageResult<Product> result = service.list(pagination);

        assertThat(result).isEqualTo(expected);
        verify(productRepository).findAll(expectedCriteria);
    }

    @Test
    void rejectsNegativePage() {
        ListRestockProductsService service = new ListRestockProductsService(productRepository);

        assertThatThrownBy(() -> service.list(new PaginationCriteria(-1, 10)))
                .isInstanceOf(InvalidPaginationException.class);
        verifyNoInteractions(productRepository);
    }

    @Test
    void rejectsPageSizeAboveLimit() {
        ListRestockProductsService service = new ListRestockProductsService(productRepository);

        assertThatThrownBy(() -> service.list(new PaginationCriteria(0, 101)))
                .isInstanceOf(InvalidPaginationException.class);
        verifyNoInteractions(productRepository);
    }
}
