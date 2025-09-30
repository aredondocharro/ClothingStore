package com.aredondocharro.ClothingStore.notification.domain.exception;

public class InvalidEmailAddressException extends RuntimeException {
    public InvalidEmailAddressException(String value) {
        super("Invalid email: " + value);
    }
}
