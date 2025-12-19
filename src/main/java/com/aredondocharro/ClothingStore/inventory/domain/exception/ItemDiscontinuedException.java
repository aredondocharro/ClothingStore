package com.aredondocharro.ClothingStore.inventory.domain.exception;

public class ItemDiscontinuedException extends RuntimeException {
    public ItemDiscontinuedException() {
        super("Inventory item is discontinued");
    }
}
