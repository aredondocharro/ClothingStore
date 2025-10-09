package com.aredondocharro.ClothingStore.identity.domain.exception;

public class SelfDemotionForbiddenException extends RuntimeException {
    public SelfDemotionForbiddenException() { super("admin cannot remove own admin role"); }
}