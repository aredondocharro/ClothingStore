package com.aredondocharro.ClothingStore.identity.domain.port.out.error;

public final class TokenMissingClaimException extends TokenVerificationException {
    public TokenMissingClaimException(String message) { super(message); }
}
