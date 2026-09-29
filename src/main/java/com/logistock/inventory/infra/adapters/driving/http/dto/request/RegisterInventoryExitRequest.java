package com.logistock.inventory.infra.adapters.driving.http.dto.request;

import com.logistock.inventory.domain.constants.InventoryConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RegisterInventoryExitRequest(
        @NotNull
        @Positive
        @Schema(example = "1")
        Long productId,

        @Min(InventoryConstants.MINIMUM_MOVEMENT_QUANTITY)
        @Schema(example = "5")
        int quantity,

        @Size(max = InventoryConstants.OBSERVATION_MAX_LENGTH)
        @Schema(example = "Customer shipment")
        String observation
) {
}
