package com.aredondocharro.ClothingStore.identity.domain.model;

import com.aredondocharro.ClothingStore.identity.domain.exception.RoleRequiredException;

public enum Role {
    USER, ADMIN;

    public static Role from(String raw) {
        if (raw == null || raw.isBlank()) throw new RoleRequiredException();
        return Role.valueOf(raw.trim().toUpperCase());
    }
}
