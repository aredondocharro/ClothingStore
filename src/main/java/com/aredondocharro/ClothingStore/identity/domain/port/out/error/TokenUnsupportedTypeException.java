package com.aredondocharro.ClothingStore.identity.domain.port.out.error;

public final class TokenUnsupportedTypeException extends TokenVerificationException {
    public TokenUnsupportedTypeException(String message) { super(message); }
}
