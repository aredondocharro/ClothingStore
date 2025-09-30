package com.aredondocharro.ClothingStore.notification.domain.exception;

public class EmailSendFailedException extends RuntimeException {
    public EmailSendFailedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
