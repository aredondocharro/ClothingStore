package com.aredondocharro.ClothingStore.inventory.domain.model;

import com.aredondocharro.ClothingStore.inventory.domain.exception.InvalidReservationException;

import java.util.Objects;

public final class ReservationReference {
    private final String value;

    private ReservationReference(String value) {
        this.value = value;
    }

    public static ReservationReference of(String raw) {
        if (raw == null) throw new InvalidReservationException("Reservation reference is required");
        String v = raw.trim();
        if (v.isBlank()) throw new InvalidReservationException("Reservation reference is required");
        if (v.length() > 80) throw new InvalidReservationException("Reservation reference max length is 80");
        return new ReservationReference(v);
    }

    public String getValue() {
        return value;
    }

    @Override public String toString() { return value; }

    @Override public int hashCode() { return Objects.hash(value); }

    @Override
    public boolean equals(Object o) {
        return (o instanceof ReservationReference other) && value.equals(other.value);
    }
}
