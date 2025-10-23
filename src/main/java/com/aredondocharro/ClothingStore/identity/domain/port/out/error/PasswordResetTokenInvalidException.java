package com.aredondocharro.ClothingStore.identity.domain.port.out.error;

public class PasswordResetTokenInvalidException extends RuntimeException {
    public PasswordResetTokenInvalidException() {
        super("Invalid or expired password reset token");
    }
    public PasswordResetTokenInvalidException(String message) {
        super(message);
    }
}
