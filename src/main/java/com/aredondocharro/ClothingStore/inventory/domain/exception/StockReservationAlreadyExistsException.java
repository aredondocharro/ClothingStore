package com.aredondocharro.ClothingStore.inventory.domain.exception;

import com.aredondocharro.ClothingStore.inventory.domain.model.ReservationReference;

public class StockReservationAlreadyExistsException extends RuntimeException {
    public StockReservationAlreadyExistsException(ReservationReference reference) {
        super("Active stock reservation already exists for reference: " + reference);
    }
}
