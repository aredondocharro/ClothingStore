package com.aredondocharro.ClothingStore.identity.domain.exception;

public class PasswordConfirmationMismatchException extends RuntimeException {
    public PasswordConfirmationMismatchException() {
        super("passwords do not match");
    }
}