package com.aredondocharro.ClothingStore.inventory.domain.port.in;

import com.aredondocharro.ClothingStore.inventory.domain.model.*;

public record InventorySearchQuery(
        String text,                 // sku/name contains
        InventoryCategory category,
        Gender gender,
        Size size,
        Fabric fabric,
        ItemStatus status
) {}
