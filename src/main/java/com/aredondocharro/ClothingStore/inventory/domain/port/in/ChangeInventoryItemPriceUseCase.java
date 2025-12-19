package com.aredondocharro.ClothingStore.inventory.domain.port.in;

import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItemId;
import com.aredondocharro.ClothingStore.inventory.domain.model.Money;

import java.time.Instant;

public interface ChangeInventoryItemPriceUseCase {
    void changePrice(InventoryItemId id, Money newPrice, Instant now);
}
