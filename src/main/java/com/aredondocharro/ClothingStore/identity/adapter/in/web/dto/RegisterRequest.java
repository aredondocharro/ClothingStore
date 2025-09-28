package com.aredondocharro.ClothingStore.identity.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registrar un nuevo usuario")
public record RegisterRequest(
        @Schema(example = "user@example.com")
        @NotBlank @Email String email,

        @Schema(example = "Secret123!")
        @NotBlank @Size(min = 8, max = 128) String password
) {}
