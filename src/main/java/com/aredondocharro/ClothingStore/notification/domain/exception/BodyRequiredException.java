package com.aredondocharro.ClothingStore.notification.domain.exception;

public class BodyRequiredException extends RuntimeException {
    public BodyRequiredException() { super("body is required"); }
}
