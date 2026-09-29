package com.logistock.inventory.configuration.beans;

import com.logistock.inventory.domain.port.in.CreateOrderUseCase;
import com.logistock.inventory.domain.port.in.GetOrderUseCase;
import com.logistock.inventory.domain.port.in.ListOrdersUseCase;
import com.logistock.inventory.domain.port.out.OrderRepositoryPort;
import com.logistock.inventory.domain.port.out.ProductRepositoryPort;
import com.logistock.inventory.domain.usecase.CreateOrderService;
import com.logistock.inventory.domain.usecase.GetOrderService;
import com.logistock.inventory.domain.usecase.ListOrdersService;
import com.logistock.inventory.infra.adapters.driven.jpa.adapter.OrderRepositoryAdapter;
import com.logistock.inventory.infra.adapters.driven.jpa.mapper.OrderEntityMapper;
import com.logistock.inventory.infra.adapters.driven.jpa.repository.SpringDataOrderRepository;
import com.logistock.inventory.infra.adapters.driven.jpa.repository.SpringDataProductRepository;
import com.logistock.inventory.infra.adapters.driving.http.controller.OrderController;
import com.logistock.inventory.infra.adapters.driving.http.mapper.OrderHttpMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;

@Configuration
public class OrderUseCaseConfiguration {

    @Bean
    OrderEntityMapper orderEntityMapper() {
        return Mappers.getMapper(OrderEntityMapper.class);
    }

    @Bean
    OrderHttpMapper orderHttpMapper() {
        return Mappers.getMapper(OrderHttpMapper.class);
    }

    @Bean
    OrderRepositoryPort orderRepositoryPort(
            SpringDataOrderRepository orderRepository,
            SpringDataProductRepository productRepository,
            OrderEntityMapper mapper
    ) {
        return new OrderRepositoryAdapter(orderRepository, productRepository, mapper);
    }

    @Bean
    CreateOrderUseCase createOrderUseCase(
            ProductRepositoryPort productRepository,
            OrderRepositoryPort orderRepository,
            Clock clock,
            TransactionTemplate transactionTemplate
    ) {
        CreateOrderUseCase service = new CreateOrderService(
                productRepository,
                orderRepository,
                clock
        );
        return command -> transactionTemplate.execute(status -> service.create(command));
    }

    @Bean
    GetOrderUseCase getOrderUseCase(OrderRepositoryPort orderRepository) {
        return new GetOrderService(orderRepository);
    }

    @Bean
    ListOrdersUseCase listOrdersUseCase(
            OrderRepositoryPort orderRepository,
            TransactionTemplate transactionTemplate
    ) {
        ListOrdersUseCase service = new ListOrdersService(orderRepository);
        return criteria -> transactionTemplate.execute(status -> service.list(criteria));
    }

    @Bean
    OrderController orderController(
            CreateOrderUseCase createOrderUseCase,
            GetOrderUseCase getOrderUseCase,
            ListOrdersUseCase listOrdersUseCase,
            OrderHttpMapper mapper
    ) {
        return new OrderController(
                createOrderUseCase,
                getOrderUseCase,
                listOrdersUseCase,
                mapper
        );
    }
}
