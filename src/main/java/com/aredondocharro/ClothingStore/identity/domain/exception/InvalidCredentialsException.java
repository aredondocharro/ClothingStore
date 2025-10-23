package com.aredondocharro.ClothingStore.identity.domain.exception;


public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String value) {
        super(value);
    }
}
