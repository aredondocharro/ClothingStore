package com.aredondocharro.ClothingStore.inventory.domain.port.out;

import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItemId;
import com.aredondocharro.ClothingStore.inventory.domain.model.ReservationReference;
import com.aredondocharro.ClothingStore.inventory.domain.model.ReservationStatus;
import com.aredondocharro.ClothingStore.inventory.domain.model.StockReservation;

import java.util.Optional;

public interface StockReservationRepositoryPort {
    Optional<StockReservation> findActiveByItemAndReference(InventoryItemId itemId, ReservationReference reference);
    void save(StockReservation reservation);

    Optional<StockReservation> findByItemAndReferenceAndStatus(
            InventoryItemId itemId,
            ReservationReference reference,
            ReservationStatus status
    );

}
