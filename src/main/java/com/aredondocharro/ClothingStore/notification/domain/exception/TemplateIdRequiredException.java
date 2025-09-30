package com.aredondocharro.ClothingStore.notification.domain.exception;

public class TemplateIdRequiredException extends RuntimeException {
    public TemplateIdRequiredException() {
        super("template id is required");
    }
}
