package com.aredondocharro.ClothingStore.identity.infrastructure.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request a password reset email")
public record ForgotPasswordRequest(
        @NotBlank @Email @Schema(example = "alice@example.com")
        String email
) {}