package com.logistock.inventory.configuration.beans;

import com.logistock.inventory.domain.port.in.RegisterInventoryEntryUseCase;
import com.logistock.inventory.domain.port.in.RegisterInventoryExitUseCase;
import com.logistock.inventory.domain.port.in.ListInventoryMovementsUseCase;
import com.logistock.inventory.domain.port.in.ListProductInventoryMovementsUseCase;
import com.logistock.inventory.domain.port.out.InventoryMovementRepositoryPort;
import com.logistock.inventory.domain.port.out.ProductRepositoryPort;
import com.logistock.inventory.domain.usecase.RegisterInventoryEntryService;
import com.logistock.inventory.domain.usecase.RegisterInventoryExitService;
import com.logistock.inventory.domain.usecase.ListInventoryMovementsService;
import com.logistock.inventory.domain.usecase.ListProductInventoryMovementsService;
import com.logistock.inventory.infra.adapters.driven.jpa.adapter.InventoryMovementRepositoryAdapter;
import com.logistock.inventory.infra.adapters.driven.jpa.mapper.InventoryMovementEntityMapper;
import com.logistock.inventory.infra.adapters.driven.jpa.repository.SpringDataInventoryMovementRepository;
import com.logistock.inventory.infra.adapters.driven.jpa.repository.SpringDataProductRepository;
import com.logistock.inventory.infra.adapters.driving.http.controller.InventoryController;
import com.logistock.inventory.infra.adapters.driving.http.mapper.InventoryMovementHttpMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;

@Configuration
public class InventoryUseCaseConfiguration {

    @Bean
    InventoryMovementEntityMapper inventoryMovementEntityMapper() {
        return Mappers.getMapper(InventoryMovementEntityMapper.class);
    }

    @Bean
    InventoryMovementHttpMapper inventoryMovementHttpMapper() {
        return Mappers.getMapper(InventoryMovementHttpMapper.class);
    }

    @Bean
    InventoryMovementRepositoryPort inventoryMovementRepositoryPort(
            SpringDataInventoryMovementRepository movementRepository,
            SpringDataProductRepository productRepository,
            InventoryMovementEntityMapper mapper
    ) {
        return new InventoryMovementRepositoryAdapter(
                movementRepository,
                productRepository,
                mapper
        );
    }

    @Bean
    RegisterInventoryEntryUseCase registerInventoryEntryUseCase(
            ProductRepositoryPort productRepository,
            InventoryMovementRepositoryPort movementRepository,
            Clock clock,
            TransactionTemplate transactionTemplate
    ) {
        RegisterInventoryEntryUseCase service = new RegisterInventoryEntryService(
                productRepository,
                movementRepository,
                clock
        );
        return command -> transactionTemplate.execute(status -> service.register(command));
    }

    @Bean
    RegisterInventoryExitUseCase registerInventoryExitUseCase(
            ProductRepositoryPort productRepository,
            InventoryMovementRepositoryPort movementRepository,
            Clock clock,
            TransactionTemplate transactionTemplate
    ) {
        RegisterInventoryExitUseCase service = new RegisterInventoryExitService(
                productRepository,
                movementRepository,
                clock
        );
        return command -> transactionTemplate.execute(status -> service.register(command));
    }

    @Bean
    ListInventoryMovementsUseCase listInventoryMovementsUseCase(
            InventoryMovementRepositoryPort movementRepository
    ) {
        return new ListInventoryMovementsService(movementRepository);
    }

    @Bean
    ListProductInventoryMovementsUseCase listProductInventoryMovementsUseCase(
            ProductRepositoryPort productRepository,
            InventoryMovementRepositoryPort movementRepository
    ) {
        return new ListProductInventoryMovementsService(
                productRepository,
                movementRepository
        );
    }

    @Bean
    InventoryController inventoryController(
            RegisterInventoryEntryUseCase registerInventoryEntryUseCase,
            RegisterInventoryExitUseCase registerInventoryExitUseCase,
            ListInventoryMovementsUseCase listInventoryMovementsUseCase,
            ListProductInventoryMovementsUseCase listProductInventoryMovementsUseCase,
            InventoryMovementHttpMapper mapper
    ) {
        return new InventoryController(
                registerInventoryEntryUseCase,
                registerInventoryExitUseCase,
                listInventoryMovementsUseCase,
                listProductInventoryMovementsUseCase,
                mapper
        );
    }
}
