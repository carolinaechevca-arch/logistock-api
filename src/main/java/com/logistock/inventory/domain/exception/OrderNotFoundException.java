package com.logistock.inventory.domain.exception;

import com.logistock.inventory.domain.constants.OrderConstants;
import lombok.Getter;

@Getter
public class OrderNotFoundException extends RuntimeException {

    private final String code = OrderConstants.ORDER_NOT_FOUND;

    public OrderNotFoundException(Long id) {
        super(OrderConstants.ORDER_NOT_FOUND_MESSAGE.formatted(id));
    }
}
