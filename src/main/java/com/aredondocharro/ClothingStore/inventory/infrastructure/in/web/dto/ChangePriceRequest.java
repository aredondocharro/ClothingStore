package com.aredondocharro.ClothingStore.inventory.infrastructure.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ChangePriceRequest(
        @NotNull BigDecimal amount,
        @NotBlank String currency
) {}
