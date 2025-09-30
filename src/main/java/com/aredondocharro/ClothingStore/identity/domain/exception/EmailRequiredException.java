package com.aredondocharro.ClothingStore.identity.domain.exception;

public class EmailRequiredException extends RuntimeException {
    public EmailRequiredException() {
        super("email is required");
    }
}