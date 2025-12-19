package com.aredondocharro.ClothingStore.inventory.domain.port.in;

import com.aredondocharro.ClothingStore.inventory.domain.model.*;

import java.time.Instant;

public record InventoryItemDetails(
        InventoryItemId id,
        Sku sku,
        ItemName name,
        String description,
        InventoryCategory category,
        AccessoryType accessoryType,
        Gender gender,
        Size size,
        Fabric fabric,
        Color color,
        Money price,
        Stock stock,
        ItemStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
