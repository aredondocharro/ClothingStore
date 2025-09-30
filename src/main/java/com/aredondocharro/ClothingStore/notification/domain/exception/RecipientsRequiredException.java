package com.aredondocharro.ClothingStore.notification.domain.exception;

public class RecipientsRequiredException extends RuntimeException {
    public RecipientsRequiredException() {
        super("at least one recipient is required");
    }
}
