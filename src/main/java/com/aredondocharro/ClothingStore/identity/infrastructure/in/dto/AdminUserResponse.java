package com.aredondocharro.ClothingStore.identity.infrastructure.in.dto;

import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

@Schema(name = "AdminUserResponse")
public record AdminUserResponse(
        UUID id,
        IdentityEmail email,
        Set<Role> roles,
        boolean enabled
) {}
