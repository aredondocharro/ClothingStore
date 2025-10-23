package com.aredondocharro.ClothingStore.identity.domain.port.out.error;

public final class TokenExpiredException extends TokenVerificationException {
    public TokenExpiredException(String message) { super(message); }
}
