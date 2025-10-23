package com.aredondocharro.ClothingStore.identity.domain.port.out.error;

public final class TokenInvalidException extends TokenVerificationException {
    public TokenInvalidException(String message) { super(message); }
    public TokenInvalidException(String message, Throwable cause) { super(message, cause); }
}
