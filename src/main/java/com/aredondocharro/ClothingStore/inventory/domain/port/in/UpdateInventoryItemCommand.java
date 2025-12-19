package com.aredondocharro.ClothingStore.inventory.domain.port.in;

import com.aredondocharro.ClothingStore.inventory.domain.model.*;

import java.time.Instant;

public record UpdateInventoryItemCommand(
        InventoryItemId id,
        ItemName name,
        String description,
        InventoryCategory category,
        AccessoryType accessoryType,
        Gender gender,
        Size size,
        Fabric fabric,
        Color color,
        Instant now
) {}
