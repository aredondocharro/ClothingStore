package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.domain.exception.CannotRemoveLastAdminException;
import com.aredondocharro.ClothingStore.identity.domain.exception.SelfDemotionForbiddenException;
import com.aredondocharro.ClothingStore.identity.domain.exception.UserNotFoundException;
import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.in.UpdateUserRolesUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.UserAdminRepositoryPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.view.UserView;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class UpdateUserRolesService implements UpdateUserRolesUseCase {

    private final UserAdminRepositoryPort repo;

    @Override
    public void setRoles(UserId userId, Set<Role> roles) {
        if (!repo.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        Set<Role> normalized = (roles == null || roles.isEmpty()) ? Set.of(Role.USER) : roles;

        boolean targetIsAdminNow = repo.hasRole(userId, Role.ADMIN);
        boolean willRemainAdmin = normalized.contains(Role.ADMIN);
        if (targetIsAdminNow && !willRemainAdmin) {
            int admins = repo.countUsersWithRole(Role.ADMIN);
            if (admins <= 1) {
                throw new CannotRemoveLastAdminException();
            }
        }

        repo.updateRoles(userId, normalized);
        log.info("Roles updated for user {}: {}", userId, normalized);
    }
}
