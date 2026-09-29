package com.logistock.inventory.configuration.beans;

import com.logistock.inventory.configuration.exceptionhandler.GlobalExceptionHandler;
import com.logistock.inventory.domain.port.in.CreateProductUseCase;
import com.logistock.inventory.domain.port.in.GetProductUseCase;
import com.logistock.inventory.domain.port.in.ListProductsUseCase;
import com.logistock.inventory.domain.port.out.ProductRepositoryPort;
import com.logistock.inventory.domain.usecase.CreateProductService;
import com.logistock.inventory.domain.usecase.GetProductService;
import com.logistock.inventory.domain.usecase.ListProductsService;
import com.logistock.inventory.infra.adapters.driven.jpa.adapter.ProductRepositoryAdapter;
import com.logistock.inventory.infra.adapters.driven.jpa.mapper.ProductEntityMapper;
import com.logistock.inventory.infra.adapters.driven.jpa.repository.SpringDataProductRepository;
import com.logistock.inventory.infra.adapters.driving.http.controller.ProductController;
import com.logistock.inventory.infra.adapters.driving.http.mapper.ProductHttpMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ProductUseCaseConfiguration {

    @Bean
    ProductEntityMapper productEntityMapper() {
        return Mappers.getMapper(ProductEntityMapper.class);
    }

    @Bean
    ProductHttpMapper productHttpMapper() {
        return Mappers.getMapper(ProductHttpMapper.class);
    }

    @Bean
    ProductRepositoryPort productRepositoryPort(
            SpringDataProductRepository repository,
            ProductEntityMapper mapper
    ) {
        return new ProductRepositoryAdapter(repository, mapper);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    GlobalExceptionHandler globalExceptionHandler(Clock clock) {
        return new GlobalExceptionHandler(clock);
    }

    @Bean
    CreateProductUseCase createProductUseCase(ProductRepositoryPort productRepository, Clock clock) {
        return new CreateProductService(productRepository, clock);
    }

    @Bean
    GetProductUseCase getProductUseCase(ProductRepositoryPort productRepository) {
        return new GetProductService(productRepository);
    }

    @Bean
    ListProductsUseCase listProductsUseCase(ProductRepositoryPort productRepository) {
        return new ListProductsService(productRepository);
    }

    @Bean
    ProductController productController(
            CreateProductUseCase createProductUseCase,
            GetProductUseCase getProductUseCase,
            ListProductsUseCase listProductsUseCase,
            ProductHttpMapper mapper
    ) {
        return new ProductController(
                createProductUseCase,
                getProductUseCase,
                listProductsUseCase,
                mapper
        );
    }
}
