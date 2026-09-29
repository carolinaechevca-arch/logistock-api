package com.logistock.inventory.domain.port.in;

import com.logistock.inventory.domain.model.PageResult;
import com.logistock.inventory.domain.model.Product;
import com.logistock.inventory.domain.model.ProductSearchCriteria;

public interface ListProductsUseCase {

    PageResult<Product> list(ProductSearchCriteria criteria);
}
