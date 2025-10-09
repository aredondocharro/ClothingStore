package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence;

import com.aredondocharro.ClothingStore.identity.domain.exception.UserNotFoundException;
import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordHash;
import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.port.out.UserAdminRepositoryPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.view.UserView;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.UserEntity;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class UserAdminRepositoryAdapter implements UserAdminRepositoryPort {

    private final SpringDataUserRepository repo;

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return repo.existsById(id);
    }
    @Override
    @Transactional
    public Optional<UserView> findById(UUID id) {
        Optional<UserEntity> opt = repo.findById(id);
        if (opt.isEmpty()) {
            log.debug("User not found id={}", id);
            return Optional.empty();
        }

        UserEntity u = opt.get();
        log.debug("User found id={} email={}", u.getId(), u.getEmail().toString());
        return Optional.of(new UserView(
                u.getId(),
                IdentityEmail.of(u.getEmail()),
                u.isEmailVerified(),
                u.getRoles()
        ));
    }

    @Override
    @Transactional
    public boolean deleteById(UUID id) {
        if (!repo.existsById(id)) {
            log.warn("Attempted to delete non-existent user with id={}", id);
            return false;
        }
        repo.deleteById(id);
        log.info("User deleted successfully: id={}", id);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasRole(UUID id, Role role) {
        return repo.userHasRole(id, role);
    }

    @Override
    @Transactional(readOnly = true)
    public int countUsersWithRole(Role role) {
        return repo.countUsersWithRole(role);
    }

    @Override
    @Transactional
    public void updateRoles(UUID id, Set<Role> roles) {
        UserEntity entity = repo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        Set<Role> oldRoles = entity.getRoles();
        entity.setRoles(roles);  // ✅ Ya es Set<Role>, no necesitas map
        repo.save(entity);


        log.info("Roles updated for user id={}: {} -> {}",
                id, oldRoles, roles.stream().map(Enum::name).collect(Collectors.toSet()));
    }
}
