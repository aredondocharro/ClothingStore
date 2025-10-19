package com.aredondocharro.ClothingStore.identity.domain.exception;

public class RefreshSessionInvalidException extends RuntimeException {
    public RefreshSessionInvalidException(String message) { super(message); }

    public static RefreshSessionInvalidException jtiRequired() {
        return new RefreshSessionInvalidException("jti is required");
    }
    public static RefreshSessionInvalidException userIdRequired() {
        return new RefreshSessionInvalidException("userId is required");
    }
    public static RefreshSessionInvalidException expiresAtRequired() {
        return new RefreshSessionInvalidException("expiresAt is required");
    }

    public static RefreshSessionInvalidException replacedByRequired(){
        return new RefreshSessionInvalidException("replacedBy is required");
    }
}