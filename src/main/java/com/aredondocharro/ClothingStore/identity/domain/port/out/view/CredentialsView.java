package com.aredondocharro.ClothingStore.identity.domain.port.out.view;

import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordHash;

import java.util.UUID;

public record CredentialsView(
        UUID id,
        IdentityEmail email,
        PasswordHash passwordHash,
        boolean emailVerified
) {}