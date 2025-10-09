package com.aredondocharro.ClothingStore.identity.domain.port.out.view;
import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.model.Role;

import java.util.Set;
import java.util.UUID;
public record UserView(UUID id, IdentityEmail email, boolean emailVerified,
                       Set<Role> roles) {}