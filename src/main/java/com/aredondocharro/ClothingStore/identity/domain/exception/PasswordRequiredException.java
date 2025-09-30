package com.aredondocharro.ClothingStore.identity.domain.exception;

public class PasswordRequiredException extends RuntimeException {
    public PasswordRequiredException() {
        super("Password is required");
    }
}
