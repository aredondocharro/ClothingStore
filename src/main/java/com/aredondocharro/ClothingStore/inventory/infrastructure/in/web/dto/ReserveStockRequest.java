package com.aredondocharro.ClothingStore.inventory.infrastructure.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReserveStockRequest(
        @NotBlank String reference,
        @Min(1) int quantity
) {}
