package com.aredondocharro.ClothingStore.inventory.domain.exception;

public class InvalidReservationException extends RuntimeException {
    public InvalidReservationException(String message) {
        super(message);
    }
}
