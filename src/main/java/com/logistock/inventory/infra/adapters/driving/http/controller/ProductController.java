package com.logistock.inventory.infra.adapters.driving.http.controller;

import com.logistock.inventory.configuration.exceptionhandler.ApiErrorResponse;
import com.logistock.inventory.domain.model.Product;
import com.logistock.inventory.domain.port.in.CreateProductUseCase;
import com.logistock.inventory.infra.adapters.driving.http.constants.ApiPaths;
import com.logistock.inventory.infra.adapters.driving.http.dto.request.CreateProductRequest;
import com.logistock.inventory.infra.adapters.driving.http.dto.response.ProductResponse;
import com.logistock.inventory.infra.adapters.driving.http.mapper.ProductHttpMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping(ApiPaths.PRODUCTS)
@Tag(name = "Products", description = "Product inventory management")
@RequiredArgsConstructor
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final ProductHttpMapper mapper;

    @PostMapping
    @Operation(summary = "Create a product")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid product data",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        Product createdProduct = createProductUseCase.create(mapper.toCommand(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path(ApiPaths.PRODUCT_BY_ID)
                .buildAndExpand(createdProduct.id())
                .toUri();
        return ResponseEntity.created(location).body(mapper.toResponse(createdProduct));
    }
}
