package com.aredondocharro.ClothingStore.identity.infrastructure.in.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendVerificationEmailRequest(
        @Email
        @NotBlank
        String email
) {}

