package com.aredondocharro.ClothingStore.identity.domain.exception;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String value) {
        super("Invalid Password: " + value);
    }
}
