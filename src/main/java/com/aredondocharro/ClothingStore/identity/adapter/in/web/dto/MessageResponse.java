package com.aredondocharro.ClothingStore.identity.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Simple message response when you register and need to verify your email")
public record MessageResponse(
        @Schema(example = "Check your email to verify your account.")
        String message
) {}
