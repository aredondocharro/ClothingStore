package com.aredondocharro.ClothingStore.identity.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credential access data")
public record LoginRequest(
        @Schema(example = "user@example.com") @NotBlank @Email String email,
        @Schema(example = "Secret123!") @NotBlank String password
) {}
