package com.aredondocharro.ClothingStore.inventory.infrastructure.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AdjustStockRequest(
        int delta,
        @NotBlank String reason
) {}
