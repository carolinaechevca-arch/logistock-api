package com.logistock.inventory.domain.exception;

import lombok.Getter;

@Getter
public class InvalidProductException extends RuntimeException {

    private final String code;

    public InvalidProductException(String code, String message) {
        super(message);
        this.code = code;
    }
}
