package com.logistock.inventory.domain.port.in;

import com.logistock.inventory.domain.model.PageResult;
import com.logistock.inventory.domain.model.PaginationCriteria;
import com.logistock.inventory.domain.model.Product;

public interface ListRestockProductsUseCase {

    PageResult<Product> list(PaginationCriteria criteria);
}
