package com.logistock.inventory.domain.usecase;

import com.logistock.inventory.domain.enums.OrderStatus;
import com.logistock.inventory.domain.exception.InvalidPaginationException;
import com.logistock.inventory.domain.model.Order;
import com.logistock.inventory.domain.model.OrderItem;
import com.logistock.inventory.domain.model.PageResult;
import com.logistock.inventory.domain.model.PaginationCriteria;
import com.logistock.inventory.domain.port.out.OrderRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListOrdersServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-28T20:00:00Z");

    @Mock
    private OrderRepositoryPort orderRepository;

    @Test
    void listsOrders() {
        PaginationCriteria criteria = new PaginationCriteria(0, 10);
        PageResult<Order> expected = new PageResult<>(
                List.of(order()),
                0,
                10,
                1,
                1,
                true
        );
        when(orderRepository.findAll(criteria)).thenReturn(expected);
        ListOrdersService service = new ListOrdersService(orderRepository);

        PageResult<Order> result = service.list(criteria);

        assertThat(result).isEqualTo(expected);
        verify(orderRepository).findAll(criteria);
    }

    @Test
    void rejectsInvalidPagination() {
        ListOrdersService service = new ListOrdersService(orderRepository);

        assertThatThrownBy(() -> service.list(new PaginationCriteria(0, 0)))
                .isInstanceOf(InvalidPaginationException.class);
        verifyNoInteractions(orderRepository);
    }

    private Order order() {
        return new Order(
                10L,
                NOW,
                OrderStatus.CREATED,
                List.of(new OrderItem(20L, 30L, 2))
        );
    }
}
