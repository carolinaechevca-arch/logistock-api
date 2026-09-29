package com.logistock.inventory.domain.usecase;

import com.logistock.inventory.domain.enums.ProductCategory;
import com.logistock.inventory.domain.exception.InvalidPaginationException;
import com.logistock.inventory.domain.exception.InvalidStockRangeException;
import com.logistock.inventory.domain.model.PageResult;
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
class ListProductsServiceTest {

    @Mock
    private ProductRepositoryPort productRepository;

    @Test
    void listsProductsWithValidCriteria() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                ProductCategory.ELECTRONICS,
                5,
                50,
                0,
                10
        );
        PageResult<Product> expected = new PageResult<>(List.of(), 0, 10, 0, 0, true);
        when(productRepository.findAll(criteria)).thenReturn(expected);
        ListProductsService service = new ListProductsService(productRepository);

        PageResult<Product> result = service.list(criteria);

        assertThat(result).isEqualTo(expected);
        verify(productRepository).findAll(criteria);
    }

    @Test
    void rejectsMinimumStockGreaterThanMaximumStock() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(null, 20, 10, 0, 10);
        ListProductsService service = new ListProductsService(productRepository);

        assertThatThrownBy(() -> service.list(criteria))
                .isInstanceOf(InvalidStockRangeException.class)
                .hasMessage("minStock must be less than or equal to maxStock");
        verifyNoInteractions(productRepository);
    }

    @Test
    void rejectsNegativeStockFilter() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(null, -1, null, 0, 10);
        ListProductsService service = new ListProductsService(productRepository);

        assertThatThrownBy(() -> service.list(criteria))
                .isInstanceOf(InvalidStockRangeException.class)
                .hasMessage("Stock filters cannot be negative");
        verifyNoInteractions(productRepository);
    }

    @Test
    void rejectsNegativePage() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(null, null, null, -1, 10);
        ListProductsService service = new ListProductsService(productRepository);

        assertThatThrownBy(() -> service.list(criteria))
                .isInstanceOf(InvalidPaginationException.class);
        verifyNoInteractions(productRepository);
    }

    @Test
    void rejectsPageSizeAboveLimit() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(null, null, null, 0, 101);
        ListProductsService service = new ListProductsService(productRepository);

        assertThatThrownBy(() -> service.list(criteria))
                .isInstanceOf(InvalidPaginationException.class);
        verifyNoInteractions(productRepository);
    }
}
