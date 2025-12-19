package com.aredondocharro.ClothingStore.inventory.domain.model;

import java.util.Objects;

public final class ItemName {
    private final String value;

    private ItemName(String value) {
        this.value = value;
    }

    public static ItemName of(String raw) {
        Objects.requireNonNull(raw, "name null");
        String v = raw.trim();
        if (v.isBlank()) throw new IllegalArgumentException("Name is required");
        if (v.length() > 120) throw new IllegalArgumentException("Name max length is 120");
        return new ItemName(v);
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof ItemName other) && value.equals(other.value);
    }
}
