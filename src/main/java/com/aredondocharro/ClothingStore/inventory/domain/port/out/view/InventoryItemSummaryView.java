package com.aredondocharro.ClothingStore.inventory.domain.port.out.view;

import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItemId;
import com.aredondocharro.ClothingStore.inventory.domain.model.ItemStatus;
import com.aredondocharro.ClothingStore.inventory.domain.model.Money;
import com.aredondocharro.ClothingStore.inventory.domain.model.Sku;

public record InventoryItemSummaryView(
        InventoryItemId id,
        Sku sku,
        String name,
        Money price,
        int onHand,
        int reserved,
        ItemStatus status
) {}
