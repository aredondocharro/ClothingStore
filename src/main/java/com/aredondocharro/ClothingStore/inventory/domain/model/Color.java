package com.aredondocharro.ClothingStore.inventory.domain.model;

import java.util.Objects;

public final class Color {
    private final String value;

    private Color(String value) {
        this.value = value;
    }

    public static Color of(String raw) {
        if (raw == null) return null; // opcional
        String v = raw.trim();
        if (v.isBlank()) return null;
        if (v.length() > 40) throw new IllegalArgumentException("Color max length is 40");
        return new Color(v);
    }

    public String getValue() {
        return value;
    }

    @Override public String toString() { return value; }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Color other) && value.equals(other.value);
    }
}
