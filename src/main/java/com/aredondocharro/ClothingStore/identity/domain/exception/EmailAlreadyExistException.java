package com.aredondocharro.ClothingStore.identity.domain.exception;

public class EmailAlreadyExistException extends RuntimeException{
    public EmailAlreadyExistException() {
        super("Email already exists");
    }
}
