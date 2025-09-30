package com.aredondocharro.ClothingStore.identity.infrastructure.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token JWT")
public record AuthResponse(
        @Schema(description = "JWT of short timing") String accessToken,
        @Schema(description = "JWT of refresh (long)") String refreshToken
) {}