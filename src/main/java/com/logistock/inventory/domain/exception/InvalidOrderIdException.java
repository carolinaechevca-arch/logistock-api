package com.logistock.inventory.domain.exception;

import com.logistock.inventory.domain.constants.OrderConstants;
import lombok.Getter;

@Getter
public class InvalidOrderIdException extends RuntimeException {

    private final String code = OrderConstants.INVALID_ORDER_ID;

    public InvalidOrderIdException() {
        super(OrderConstants.INVALID_ORDER_ID_MESSAGE);
    }
}
