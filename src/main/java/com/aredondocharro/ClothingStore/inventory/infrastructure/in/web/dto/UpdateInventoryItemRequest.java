package com.aredondocharro.ClothingStore.inventory.infrastructure.in.web.dto;

import com.aredondocharro.ClothingStore.inventory.domain.model.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateInventoryItemRequest(
        @NotBlank String name,
        String description,
        @NotNull InventoryCategory category,
        @NotNull AccessoryType accessoryType,
        @NotNull Gender gender,
        @NotNull Size size,
        @NotNull Fabric fabric,
        String color
) {}
