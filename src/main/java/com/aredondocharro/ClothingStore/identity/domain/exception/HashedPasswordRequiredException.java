package com.aredondocharro.ClothingStore.identity.domain.exception;

public class HashedPasswordRequiredException extends RuntimeException {
    public HashedPasswordRequiredException() {
        super("Password is not hashed");
    }
}
