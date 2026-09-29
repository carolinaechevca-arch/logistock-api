package com.logistock.inventory.domain.exception;

import com.logistock.inventory.domain.constants.OrderConstants;
import lombok.Getter;

@Getter
public class InvalidOrderException extends RuntimeException {

    private final String code = OrderConstants.INVALID_ORDER;

    public InvalidOrderException(String message) {
        super(message);
    }
}
