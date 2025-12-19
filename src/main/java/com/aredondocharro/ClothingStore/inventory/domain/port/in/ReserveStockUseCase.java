package com.aredondocharro.ClothingStore.inventory.domain.port.in;

import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItemId;
import com.aredondocharro.ClothingStore.inventory.domain.model.ReservationReference;
import com.aredondocharro.ClothingStore.inventory.domain.model.StockReservationId;

import java.time.Instant;

public interface ReserveStockUseCase {
    StockReservationId reserve(InventoryItemId itemId, ReservationReference reference, int quantity, Instant now);
}
