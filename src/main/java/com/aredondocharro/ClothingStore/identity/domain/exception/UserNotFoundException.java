package com.aredondocharro.ClothingStore.identity.domain.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    private final UUID userId;

    public UserNotFoundException(UUID userId) {
        super("User not found: " + userId);
        this.userId = userId;
    }

    public UserNotFoundException(String message) {
        super(message);
        this.userId = null;
    }

    public UUID getUserId() {
        return userId;
    }
}
