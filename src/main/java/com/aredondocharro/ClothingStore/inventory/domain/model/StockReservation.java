package com.aredondocharro.ClothingStore.inventory.domain.model;

import com.aredondocharro.ClothingStore.inventory.domain.exception.InvalidReservationException;

import java.time.Instant;
import java.util.Objects;

public record StockReservation(
        StockReservationId id,
        InventoryItemId itemId,
        ReservationReference reference,
        int quantity,
        ReservationStatus status,
        Instant createdAt,
        Instant releasedAt,
        Instant consumedAt
) {

    public StockReservation {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(itemId, "itemId");
        Objects.requireNonNull(reference, "reference");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(createdAt, "createdAt");

        if (quantity <= 0) throw new InvalidReservationException("Reservation quantity must be > 0");

        if (status == ReservationStatus.RELEASED && releasedAt == null) {
            throw new InvalidReservationException("releasedAt is required when status is RELEASED");
        }
        if (status == ReservationStatus.CONSUMED && consumedAt == null) {
            throw new InvalidReservationException("consumedAt is required when status is CONSUMED");
        }
    }

    public static StockReservation createNew(
            StockReservationId id,
            InventoryItemId itemId,
            ReservationReference reference,
            int quantity,
            Instant now
    ) {
        Objects.requireNonNull(now, "now");
        return new StockReservation(
                id == null ? StockReservationId.newId() : id,
                itemId,
                reference,
                quantity,
                ReservationStatus.ACTIVE,
                now,
                null,
                null
        );
    }

    public boolean isActive() {
        return status == ReservationStatus.ACTIVE;
    }

    public StockReservation release(Instant now) {
        Objects.requireNonNull(now, "now");
        if (status != ReservationStatus.ACTIVE) return this;

        return new StockReservation(
                id, itemId, reference, quantity,
                ReservationStatus.RELEASED,
                createdAt,
                now,
                null
        );
    }

    public StockReservation consume(Instant now) {
        Objects.requireNonNull(now, "now");
        if (status != ReservationStatus.ACTIVE) return this;

        return new StockReservation(
                id, itemId, reference, quantity,
                ReservationStatus.CONSUMED,
                createdAt,
                null,
                now
        );
    }
}
