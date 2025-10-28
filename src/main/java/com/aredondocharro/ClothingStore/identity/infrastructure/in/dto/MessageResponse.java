package com.aredondocharro.ClothingStore.identity.infrastructure.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "MessageResponse",
        description = "Simple one-field payload with a human-readable message."
)
public record MessageResponse(
        @Schema(description = "Human-readable message")
        String message
) {}