package com.logistock.inventory.infra.adapters.driving.http.controller;

import com.logistock.inventory.configuration.exceptionhandler.ApiErrorResponse;
import com.logistock.inventory.domain.model.InventoryMovement;
import com.logistock.inventory.domain.model.PageResult;
import com.logistock.inventory.domain.port.in.ListInventoryMovementsUseCase;
import com.logistock.inventory.domain.port.in.RegisterInventoryEntryUseCase;
import com.logistock.inventory.domain.port.in.RegisterInventoryExitUseCase;
import com.logistock.inventory.infra.adapters.driving.http.constants.ApiPaths;
import com.logistock.inventory.infra.adapters.driving.http.dto.request.RegisterInventoryEntryRequest;
import com.logistock.inventory.infra.adapters.driving.http.dto.request.RegisterInventoryExitRequest;
import com.logistock.inventory.infra.adapters.driving.http.dto.response.InventoryMovementResponse;
import com.logistock.inventory.infra.adapters.driving.http.dto.request.PaginationRequest;
import com.logistock.inventory.infra.adapters.driving.http.dto.response.PageResponse;
import com.logistock.inventory.infra.adapters.driving.http.mapper.InventoryMovementHttpMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.INVENTORY)
@Tag(name = "Inventory", description = "Inventory movement management")
@RequiredArgsConstructor
public class InventoryController {

    private final RegisterInventoryEntryUseCase registerInventoryEntryUseCase;
    private final RegisterInventoryExitUseCase registerInventoryExitUseCase;
    private final ListInventoryMovementsUseCase listInventoryMovementsUseCase;
    private final InventoryMovementHttpMapper mapper;

    @PostMapping(ApiPaths.INVENTORY_ENTRIES)
    @Operation(summary = "Register an inventory entry")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inventory entry registered"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid inventory entry",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<InventoryMovementResponse> registerEntry(
            @Valid @RequestBody RegisterInventoryEntryRequest request
    ) {
        InventoryMovement movement = registerInventoryEntryUseCase.register(
                mapper.toCommand(request)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(movement));
    }

    @PostMapping(ApiPaths.INVENTORY_EXITS)
    @Operation(summary = "Register an inventory exit")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inventory exit registered"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid inventory exit",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Insufficient stock or concurrent update",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<InventoryMovementResponse> registerExit(
            @Valid @RequestBody RegisterInventoryExitRequest request
    ) {
        InventoryMovement movement = registerInventoryExitUseCase.register(
                mapper.toExitCommand(request)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(movement));
    }

    @GetMapping(ApiPaths.INVENTORY_MOVEMENTS)
    @Operation(summary = "List inventory movements")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inventory movements listed"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<PageResponse<InventoryMovementResponse>> listMovements(
            @ParameterObject @ModelAttribute PaginationRequest request
    ) {
        PageResult<InventoryMovement> result = listInventoryMovementsUseCase.list(
                mapper.toPaginationCriteria(request)
        );
        return ResponseEntity.ok(mapper.toPageResponse(result));
    }
}
