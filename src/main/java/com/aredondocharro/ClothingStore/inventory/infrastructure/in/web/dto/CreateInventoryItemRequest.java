package com.aredondocharro.ClothingStore.inventory.infrastructure.in.web.dto;

import com.aredondocharro.ClothingStore.inventory.domain.model.*;

import com.aredondocharro.ClothingStore.inventory.domain.model.Size;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;

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
        @NotNull @DecimalMin(value = "0.00", inclusive = true) BigDecimal priceAmount,
        @NotBlank @JsonAlias("currencyCode") String priceCurrency,
        @Min(0) @JsonAlias("initialStock") int initialOnHand
) {}

