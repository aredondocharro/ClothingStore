package com.aredondocharro.ClothingStore.identity.domain.exception;

public class InvalidEmailFormatException extends RuntimeException {
    public InvalidEmailFormatException(String value) {
        super("Invalid email format: " + value);
    }
}

