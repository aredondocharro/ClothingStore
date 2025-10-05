package com.aredondocharro.ClothingStore.identity.infrastructure.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;


@Schema(description = "Authenticated password change")
public record ChangePasswordRequest(
        @NotBlank @Schema(example = "OldSecret!")
        String currentPassword,
        @NotBlank @Schema(example = "NewStrongPassword123!")
        String newPassword
) {}