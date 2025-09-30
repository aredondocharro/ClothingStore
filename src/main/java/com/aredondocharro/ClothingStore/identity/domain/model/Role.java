package com.aredondocharro.ClothingStore.identity.domain.model;

public enum Role {
    USER, ADMIN;

    public static Role from(String raw) {
        if (raw == null || raw.isBlank()) throw new IllegalArgumentException("role is required");
        return Role.valueOf(raw.trim().toUpperCase());
    }
}
