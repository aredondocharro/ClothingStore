package com.aredondocharro.ClothingStore.inventory.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class InventoryItemId {
    private final UUID value;

    private InventoryItemId(UUID value) {
        this.value = Objects.requireNonNull(value, "InventoryItemId null");
    }

    public static InventoryItemId newId() {
        return new InventoryItemId(UUID.randomUUID());
    }

    public static InventoryItemId of(UUID value) {
        return new InventoryItemId(value);
    }

    public static InventoryItemId of(String value) {
        return new InventoryItemId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof InventoryItemId other) && value.equals(other.value);
    }
}
