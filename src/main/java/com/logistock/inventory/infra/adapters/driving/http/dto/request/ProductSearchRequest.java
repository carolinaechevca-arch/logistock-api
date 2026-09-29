package com.logistock.inventory.infra.adapters.driving.http.dto.request;

import com.logistock.inventory.domain.constants.ProductConstants;
import com.logistock.inventory.domain.enums.ProductCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductSearchRequest {

    @Schema(example = "ELECTRONICS", description = "Optional product category")
    private ProductCategory category;

    @Schema(example = "5", description = "Minimum stock, inclusive")
    private Integer minStock;

    @Schema(example = "50", description = "Maximum stock, inclusive")
    private Integer maxStock;

    @Schema(example = "0", defaultValue = "0")
    private int page = ProductConstants.DEFAULT_PAGE;

    @Schema(example = "10", defaultValue = "10")
    private int size = ProductConstants.DEFAULT_PAGE_SIZE;
}
