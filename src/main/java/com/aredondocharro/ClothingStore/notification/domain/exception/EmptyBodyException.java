package com.aredondocharro.ClothingStore.notification.domain.exception;

public class EmptyBodyException extends RuntimeException {
    public EmptyBodyException() {
        super("'body' is required");
    }
}
