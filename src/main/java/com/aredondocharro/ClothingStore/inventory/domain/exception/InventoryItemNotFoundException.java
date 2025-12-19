package com.aredondocharro.ClothingStore.inventory.domain.exception;

import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItemId;

public class InventoryItemNotFoundException extends RuntimeException {
    public InventoryItemNotFoundException(InventoryItemId id) {
        super("Inventory item not found: " + id);
    }
}
