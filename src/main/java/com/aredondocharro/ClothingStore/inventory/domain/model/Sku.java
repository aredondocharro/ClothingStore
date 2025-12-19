package com.aredondocharro.ClothingStore.inventory.domain.model;

import com.aredondocharro.ClothingStore.inventory.domain.exception.InvalidSkuException;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Sku {
    // Ejemplo razonable: letras/números, guion, underscore, punto. 3..64 chars.
    private static final Pattern P = Pattern.compile("^[A-Z0-9][A-Z0-9._-]{2,63}$");

    private final String value;

    private Sku(String value) {
        this.value = value;
    }

    public static Sku of(String raw) {
        if (raw == null) throw new InvalidSkuException("SKU is required");
        String v = raw.trim().toUpperCase();
        if (v.isBlank()) throw new InvalidSkuException("SKU is required");
        if (!P.matcher(v).matches()) {
            throw new InvalidSkuException("Invalid SKU format. Expected 3..64 chars [A-Z0-9._-], starting with alphanumeric");
        }
        return new Sku(v);
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
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Sku other) && value.equals(other.value);
    }
}
