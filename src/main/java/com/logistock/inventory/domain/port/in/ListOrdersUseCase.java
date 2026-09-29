package com.logistock.inventory.domain.port.in;

import com.logistock.inventory.domain.model.Order;
import com.logistock.inventory.domain.model.PageResult;
import com.logistock.inventory.domain.model.PaginationCriteria;

public interface ListOrdersUseCase {

    PageResult<Order> list(PaginationCriteria criteria);
}
