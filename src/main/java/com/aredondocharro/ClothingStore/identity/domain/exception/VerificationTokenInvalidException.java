package com.aredondocharro.ClothingStore.identity.domain.exception;

public class VerificationTokenInvalidException extends RuntimeException {
    public VerificationTokenInvalidException(String msg) { super(msg); }
}
