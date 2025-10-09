package com.aredondocharro.ClothingStore.identity.infrastructure.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

@Schema(name = "AdminSetRolesRequest", description = "New complete set of roles for the user")
public record AdminSetRolesRequest(
        @NotEmpty
        @Schema(description = "Roles to assign", example = "[\"USER\",\"ADMIN\"]",
                allowableValues = {"USER","ADMIN"})
        Set<String> roles
) {}
