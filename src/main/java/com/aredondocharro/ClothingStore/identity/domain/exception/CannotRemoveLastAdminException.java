package com.aredondocharro.ClothingStore.identity.domain.exception;

public class CannotRemoveLastAdminException extends RuntimeException {
    public CannotRemoveLastAdminException() { super("cannot remove last admin"); }
}