package com.logistock.inventory.infra.adapters.driving.http.dto.request;

import com.logistock.inventory.domain.constants.ProductConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaginationRequest {

    @Schema(example = "0", defaultValue = "0")
    private int page = ProductConstants.DEFAULT_PAGE;

    @Schema(example = "10", defaultValue = "10")
    private int size = ProductConstants.DEFAULT_PAGE_SIZE;
}
