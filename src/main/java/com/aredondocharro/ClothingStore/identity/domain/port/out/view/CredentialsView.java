package com.aredondocharro.ClothingStore.identity.domain.port.out.view;

import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordHash;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;


public record CredentialsView(
        UserId id,
        IdentityEmail email,
        PasswordHash passwordHash,
        boolean emailVerified
) {}