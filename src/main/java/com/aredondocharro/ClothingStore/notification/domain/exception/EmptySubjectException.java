package com.aredondocharro.ClothingStore.notification.domain.exception;

public class EmptySubjectException extends RuntimeException {
    public EmptySubjectException() {
        super("'subject' is required");
    }
}
