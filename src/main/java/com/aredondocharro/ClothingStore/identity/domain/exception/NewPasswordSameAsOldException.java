package com.aredondocharro.ClothingStore.identity.domain.exception;

public class NewPasswordSameAsOldException extends RuntimeException {
    public NewPasswordSameAsOldException() {
        super("new password cannot be the same as the current password");
    }
    public NewPasswordSameAsOldException(String message) {
        super(message);
    }
}
