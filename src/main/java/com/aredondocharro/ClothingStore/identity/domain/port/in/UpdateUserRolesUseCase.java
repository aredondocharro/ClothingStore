package com.aredondocharro.ClothingStore.identity.domain.port.in;

import com.aredondocharro.ClothingStore.identity.domain.model.Role;

import java.util.Set;
import java.util.UUID;

public interface UpdateUserRolesUseCase {
    void setRoles(UUID userId, Set<Role> roles);
}

