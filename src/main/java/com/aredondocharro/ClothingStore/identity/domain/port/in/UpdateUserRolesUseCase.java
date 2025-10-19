package com.aredondocharro.ClothingStore.identity.domain.port.in;

import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;

import java.util.Set;


public interface UpdateUserRolesUseCase {
    void setRoles(UserId userId, Set<Role> roles);
}

