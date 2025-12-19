package com.aredondocharro.ClothingStore.inventory.domain.port.in;

import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItemId;

public interface GetInventoryItemUseCase {
    InventoryItemDetails getById(InventoryItemId id);
}
