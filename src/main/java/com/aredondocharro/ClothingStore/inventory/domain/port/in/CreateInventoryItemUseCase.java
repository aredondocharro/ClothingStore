package com.aredondocharro.ClothingStore.inventory.domain.port.in;

import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItemId;

public interface CreateInventoryItemUseCase {
    InventoryItemId create(CreateInventoryItemCommand command);
}
