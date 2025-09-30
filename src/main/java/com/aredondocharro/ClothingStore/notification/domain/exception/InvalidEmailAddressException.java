package com.aredondocharro.ClothingStore.notification.domain.exception;

public class InvalidEmailAddressException extends RuntimeException {
    public InvalidEmailAddressException() {
        super("invalid email address");
    }
    public InvalidEmailAddressException(String value) {
        super("invalid email address");
    }
}