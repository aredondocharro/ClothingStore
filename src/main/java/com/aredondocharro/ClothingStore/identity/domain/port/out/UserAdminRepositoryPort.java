package com.aredondocharro.ClothingStore.identity.domain.port.out;

import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.out.view.UserView;

import java.util.Optional;
import java.util.Set;

public interface UserAdminRepositoryPort {
    boolean existsById(UserId id);
    boolean deleteById(UserId id);
    boolean hasRole(UserId id, Role role);
    int countUsersWithRole(Role role);
    void updateRoles(UserId id, Set<Role> roles);
    Optional<UserView>findById(UserId id);
}
