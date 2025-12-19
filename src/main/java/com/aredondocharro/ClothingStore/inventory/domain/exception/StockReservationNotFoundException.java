package com.aredondocharro.ClothingStore.inventory.domain.exception;

import com.aredondocharro.ClothingStore.inventory.domain.model.ReservationReference;

public class StockReservationNotFoundException extends RuntimeException {
    public StockReservationNotFoundException(ReservationReference reference) {
        super("Stock reservation not found for reference: " + reference);
    }
}
