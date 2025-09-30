package com.aredondocharro.ClothingStore.notification.domain.exception;

public class SubjectRequiredException extends RuntimeException {
    public SubjectRequiredException() { super("subject is required"); }
}
