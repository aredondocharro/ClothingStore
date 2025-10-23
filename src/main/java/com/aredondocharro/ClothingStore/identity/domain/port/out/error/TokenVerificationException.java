package com.aredondocharro.ClothingStore.identity.domain.port.out.error;

public abstract class TokenVerificationException extends RuntimeException {
    public TokenVerificationException(String message) { super(message); }
    public TokenVerificationException(String message, Throwable cause) { super(message, cause); }
}