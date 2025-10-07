package com.aredondocharro.ClothingStore.identity.domain.exception;

public class RoleRequiredException extends RuntimeException {
    public RoleRequiredException() { super("role is required"); }
}