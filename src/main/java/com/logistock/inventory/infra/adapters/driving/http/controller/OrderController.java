package com.logistock.inventory.infra.adapters.driving.http.controller;

import com.logistock.inventory.configuration.exceptionhandler.ApiErrorResponse;
import com.logistock.inventory.domain.model.Order;
import com.logistock.inventory.domain.port.in.CreateOrderUseCase;
import com.logistock.inventory.infra.adapters.driving.http.constants.ApiPaths;
import com.logistock.inventory.infra.adapters.driving.http.dto.request.CreateOrderRequest;
import com.logistock.inventory.infra.adapters.driving.http.dto.response.OrderResponse;
import com.logistock.inventory.infra.adapters.driving.http.mapper.OrderHttpMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.ORDERS)
@Tag(name = "Orders", description = "Order management")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final OrderHttpMapper mapper;

    @PostMapping
    @Operation(summary = "Create an order")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid order",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Insufficient stock",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        Order order = createOrderUseCase.create(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(order));
    }
}
