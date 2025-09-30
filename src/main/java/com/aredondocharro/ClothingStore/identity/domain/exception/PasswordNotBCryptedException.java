package com.aredondocharro.ClothingStore.identity.domain.exception;

public class PasswordNotBCryptedException extends RuntimeException{
    public PasswordNotBCryptedException() {
        super("Password is not bcrypted");
    }
}
