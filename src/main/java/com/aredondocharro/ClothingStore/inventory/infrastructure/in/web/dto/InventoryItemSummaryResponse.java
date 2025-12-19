package com.aredondocharro.ClothingStore.inventory.infrastructure.in.web.dto;

import com.aredondocharro.ClothingStore.inventory.domain.model.ItemStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record InventoryItemSummaryResponse(
        UUID id,
        String sku,
        String name,
        BigDecimal priceAmount,
        String priceCurrency,
        int onHand,
        int reserved,
        int available,
        ItemStatus status
) {}
