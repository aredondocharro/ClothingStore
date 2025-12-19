package com.aredondocharro.ClothingStore.inventory.infrastructure.in.web.dto;

import com.aredondocharro.ClothingStore.inventory.domain.model.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateInventoryItemRequest(
        @NotBlank String sku,
        @NotBlank String name,
        String description,
        @NotNull InventoryCategory category,
        @NotNull AccessoryType accessoryType,
        @NotNull Gender gender,
        @NotNull Size size,
        @NotNull Fabric fabric,
        String color,
        @NotNull BigDecimal priceAmount,
        @NotBlank String priceCurrency,
        @Min(0) int initialOnHand
) {}
