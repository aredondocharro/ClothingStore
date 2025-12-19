package com.aredondocharro.ClothingStore.inventory.domain.port.in;

import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItemId;
import com.aredondocharro.ClothingStore.inventory.domain.model.ReservationReference;

import java.time.Instant;

public interface ConsumeStockUseCase {
    void consume(InventoryItemId itemId, ReservationReference reference, Instant now);
}
