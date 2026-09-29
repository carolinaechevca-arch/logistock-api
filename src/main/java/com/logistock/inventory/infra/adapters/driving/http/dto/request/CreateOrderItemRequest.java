package com.logistock.inventory.infra.adapters.driving.http.dto.request;

import com.logistock.inventory.domain.constants.OrderConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderItemRequest(
        @NotNull
        @Positive
        @Schema(example = "1")
        Long productId,

        @Min(OrderConstants.MINIMUM_ITEM_QUANTITY)
        @Schema(example = "2")
        int quantity
) {
}
