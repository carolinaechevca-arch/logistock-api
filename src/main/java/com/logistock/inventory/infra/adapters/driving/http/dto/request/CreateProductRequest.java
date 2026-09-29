package com.logistock.inventory.infra.adapters.driving.http.dto.request;

import com.logistock.inventory.domain.constants.ProductConstants;
import com.logistock.inventory.domain.enums.ProductCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank
        @Size(max = ProductConstants.NAME_MAX_LENGTH)
        @Schema(example = "Barcode scanner")
        String name,

        @Size(max = ProductConstants.DESCRIPTION_MAX_LENGTH)
        @Schema(example = "Handheld scanner used in warehouse operations")
        String description,

        @NotNull
        @Schema(example = "ELECTRONICS")
        ProductCategory category,

        @Min(ProductConstants.MINIMUM_STOCK)
        @Schema(example = "12")
        int stock,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        @Schema(example = "245.90")
        BigDecimal price
) {
}
