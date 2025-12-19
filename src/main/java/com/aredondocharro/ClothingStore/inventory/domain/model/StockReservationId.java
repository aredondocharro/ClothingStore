package com.aredondocharro.ClothingStore.inventory.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class StockReservationId {
    private final UUID value;

    private StockReservationId(UUID value) {
        this.value = Objects.requireNonNull(value, "StockReservationId null");
    }

    public static StockReservationId newId() {
        return new StockReservationId(UUID.randomUUID());
    }

    public static StockReservationId of(UUID value) {
        return new StockReservationId(value);
    }

    public static StockReservationId of(String value) {
        return new StockReservationId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override public String toString() { return value.toString(); }

    @Override public int hashCode() { return value.hashCode(); }

    @Override
    public boolean equals(Object o) {
        return (o instanceof StockReservationId other) && value.equals(other.value);
    }
}
