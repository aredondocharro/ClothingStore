package com.aredondocharro.ClothingStore.identity.domain.port.out;

import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.port.out.view.UserView;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserAdminRepositoryPort {
    boolean existsById(UUID id);
    boolean deleteById(UUID id);
    boolean hasRole(UUID id, Role role);
    int countUsersWithRole(Role role);
    void updateRoles(UUID id, Set<Role> roles);
    Optional<UserView>findById(UUID id);
}
