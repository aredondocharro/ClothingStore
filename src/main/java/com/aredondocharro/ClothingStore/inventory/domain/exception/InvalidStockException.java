package com.aredondocharro.ClothingStore.inventory.domain.exception;

public class InvalidStockException extends RuntimeException {
    public InvalidStockException(String message) {
        super(message);
    }
}
