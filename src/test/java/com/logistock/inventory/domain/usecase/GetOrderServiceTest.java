package com.logistock.inventory.domain.usecase;

import com.logistock.inventory.domain.enums.OrderStatus;
import com.logistock.inventory.domain.exception.InvalidOrderIdException;
import com.logistock.inventory.domain.exception.OrderNotFoundException;
import com.logistock.inventory.domain.model.Order;
import com.logistock.inventory.domain.model.OrderItem;
import com.logistock.inventory.domain.port.out.OrderRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOrderServiceTest {

    private static final long ORDER_ID = 10L;
    private static final Instant NOW = Instant.parse("2026-09-28T20:00:00Z");

    @Mock
    private OrderRepositoryPort orderRepository;

    @Test
    void getsOrderById() {
        Order order = order();
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        GetOrderService service = new GetOrderService(orderRepository);

        Order result = service.getById(ORDER_ID);

        assertThat(result).isEqualTo(order);
        verify(orderRepository).findById(ORDER_ID);
    }

    @Test
    void rejectsInvalidOrderId() {
        GetOrderService service = new GetOrderService(orderRepository);

        assertThatThrownBy(() -> service.getById(0L))
                .isInstanceOf(InvalidOrderIdException.class)
                .hasMessage("Order id must be greater than zero");
        verifyNoInteractions(orderRepository);
    }

    @Test
    void throwsExceptionWhenOrderDoesNotExist() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());
        GetOrderService service = new GetOrderService(orderRepository);

        assertThatThrownBy(() -> service.getById(ORDER_ID))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found with id 10");
        verify(orderRepository).findById(ORDER_ID);
    }

    private Order order() {
        return new Order(
                ORDER_ID,
                NOW,
                OrderStatus.CREATED,
                List.of(new OrderItem(20L, 30L, 2))
        );
    }
}
