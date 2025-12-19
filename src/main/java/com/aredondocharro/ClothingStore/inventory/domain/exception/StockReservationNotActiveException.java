package com.aredondocharro.ClothingStore.inventory.domain.exception;

import com.aredondocharro.ClothingStore.inventory.domain.model.ReservationReference;
import com.aredondocharro.ClothingStore.inventory.domain.model.ReservationStatus;

public class StockReservationNotActiveException extends RuntimeException {
    public StockReservationNotActiveException(ReservationReference reference, ReservationStatus status) {
        super("Stock reservation is not ACTIVE for reference: " + reference + " (status=" + status + ")");
    }
}
