package com.aredondocharro.ClothingStore.identity.infrastructure.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;


@Schema(description = "Complete the reset with the provided token")
public record ResetPasswordRequest(
        @NotBlank @Schema(example = "NewStrongPassword123!")
        String newPassword
) {}