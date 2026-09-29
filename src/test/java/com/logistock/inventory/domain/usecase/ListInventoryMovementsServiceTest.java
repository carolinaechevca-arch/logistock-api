package com.logistock.inventory.domain.usecase;

import com.logistock.inventory.domain.exception.InvalidPaginationException;
import com.logistock.inventory.domain.model.InventoryMovement;
import com.logistock.inventory.domain.model.PageResult;
import com.logistock.inventory.domain.model.PaginationCriteria;
import com.logistock.inventory.domain.port.out.InventoryMovementRepositoryPort;
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
class ListInventoryMovementsServiceTest {

    @Mock
    private InventoryMovementRepositoryPort movementRepository;

    @Test
    void listsInventoryMovements() {
        PaginationCriteria criteria = new PaginationCriteria(0, 10);
        PageResult<InventoryMovement> expected = new PageResult<>(List.of(), 0, 10, 0, 0, true);
        when(movementRepository.findAll(criteria)).thenReturn(expected);
        ListInventoryMovementsService service = new ListInventoryMovementsService(
                movementRepository
        );

        PageResult<InventoryMovement> result = service.list(criteria);

        assertThat(result).isEqualTo(expected);
        verify(movementRepository).findAll(criteria);
    }

    @Test
    void rejectsNegativePage() {
        ListInventoryMovementsService service = new ListInventoryMovementsService(
                movementRepository
        );

        assertThatThrownBy(() -> service.list(new PaginationCriteria(-1, 10)))
                .isInstanceOf(InvalidPaginationException.class);
        verifyNoInteractions(movementRepository);
    }

    @Test
    void rejectsPageSizeAboveLimit() {
        ListInventoryMovementsService service = new ListInventoryMovementsService(
                movementRepository
        );

        assertThatThrownBy(() -> service.list(new PaginationCriteria(0, 101)))
                .isInstanceOf(InvalidPaginationException.class);
        verifyNoInteractions(movementRepository);
    }
}
