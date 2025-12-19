package com.aredondocharro.ClothingStore.inventory.infrastructure.in.web.dto;

import com.aredondocharro.ClothingStore.inventory.domain.model.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InventoryItemResponse(
        UUID id,
        String sku,
        String name,
        String description,
        InventoryCategory category,
        AccessoryType accessoryType,
        Gender gender,
        Size size,
        Fabric fabric,
        String color,
        BigDecimal priceAmount,
        String priceCurrency,
        int onHand,
        int reserved,
        int available,
        ItemStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
