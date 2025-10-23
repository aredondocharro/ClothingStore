package com.aredondocharro.ClothingStore.identity.domain.port.out.error;

public class VerificationTokenInvalidException extends RuntimeException {
    public VerificationTokenInvalidException(String msg) {
        super(msg);
    }
}
