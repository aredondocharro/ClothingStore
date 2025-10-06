package com.aredondocharro.ClothingStore.identity.domain.exception;

public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException() { super("passwords do not match"); }
}
