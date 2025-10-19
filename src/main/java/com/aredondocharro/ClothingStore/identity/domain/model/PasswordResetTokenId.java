// src/main/java/com/aredondocharro/ClothingStore/identity/domain/model/PasswordResetTokenId.java
package com.aredondocharro.ClothingStore.identity.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class PasswordResetTokenId {
    private final UUID value;

    private PasswordResetTokenId(UUID value) {
        this.value = Objects.requireNonNull(value, "PasswordResetTokenId null");
        if (new UUID(0L, 0L).equals(value)) {
            throw new IllegalArgumentException("Nil UUID not allowed");
        }
    }

    public static PasswordResetTokenId newId() { return new PasswordResetTokenId(UUID.randomUUID()); }
    public static PasswordResetTokenId of(UUID value) { return new PasswordResetTokenId(value); }
    public UUID value() { return value; }

    @Override public String toString() { return value.toString(); }
    @Override public int hashCode() { return value.hashCode(); }
    @Override public boolean equals(Object o) { return (o instanceof PasswordResetTokenId t) && t.value.equals(this.value); }
}
