package com.aredondocharro.ClothingStore.inventory.domain.port.in;

import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItemId;

import java.time.Instant;

public interface DiscontinueInventoryItemUseCase {
    void discontinue(InventoryItemId id, Instant now);
    void reactivate(InventoryItemId id, Instant now);
}
