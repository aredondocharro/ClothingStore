package com.aredondocharro.ClothingStore.inventory.domain.port.in;

import com.aredondocharro.ClothingStore.inventory.domain.model.*;

public record InventoryItemSummary(
        InventoryItemId id,
        Sku sku,
        ItemName name,
        Money price,
        Stock stock,
        ItemStatus status
) {}
