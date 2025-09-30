package com.aredondocharro.ClothingStore.notification.domain.exception;

public class EmailSendFailedException extends RuntimeException {
    public EmailSendFailedException(String message, Throwable cause) {
        super(message, cause);
    }
    public EmailSendFailedException(Throwable cause) {
        super("email send failed", cause);
    }
}