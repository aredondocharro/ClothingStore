package com.aredondocharro.ClothingStore.inventory.domain.port.in;

import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItemId;

import java.time.Instant;

public interface AdjustInventoryStockUseCase {
    void adjustOnHand(InventoryItemId id, int delta, String reason, Instant now);
}
