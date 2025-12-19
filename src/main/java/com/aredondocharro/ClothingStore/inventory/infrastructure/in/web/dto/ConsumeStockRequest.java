package com.aredondocharro.ClothingStore.inventory.infrastructure.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ConsumeStockRequest(
        @NotBlank String reference
) {}
