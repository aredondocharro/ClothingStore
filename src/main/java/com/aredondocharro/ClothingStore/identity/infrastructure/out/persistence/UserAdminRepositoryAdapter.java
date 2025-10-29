package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence;

import com.aredondocharro.ClothingStore.identity.domain.exception.UserNotFoundException;
import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.out.UserAdminRepositoryPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.view.UserView;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.UserEntity;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY) // exige TX abierta por el wrapper
public class UserAdminRepositoryAdapter implements UserAdminRepositoryPort {

    private final SpringDataUserRepository repo;

    @Override
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY)
    public boolean existsById(UserId id) {
        return repo.existsById(id.value());
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY)
    public Optional<UserView> findById(UserId id) {
        Optional<UserEntity> opt = repo.findById(id.value());
        if (opt.isEmpty()) {
            log.debug("User not found id={}", id);
            return Optional.empty();
        }

        UserEntity u = opt.get();
        log.debug("User found id={} email={}", u.getId(), u.getEmail());
        return Optional.of(new UserView(
                u.getId(),
                IdentityEmail.of(u.getEmail()),
                u.isEmailVerified(),
                u.getRoles()
        ));
    }

    @Override
    public boolean deleteById(UserId id) {
        if (!repo.existsById(id.value())) {
            log.warn("Attempted to delete non-existent user with id={}", id);
            return false;
        }
        repo.deleteById(id.value());
        log.info("User deleted successfully: id={}", id);
        return true;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY)
    public boolean hasRole(UserId id, Role role) {
        return repo.userHasRole(id.value(), role);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY)
    public int countUsersWithRole(Role role) {
        return repo.countUsersWithRole(role);
    }

    @Override
    public void updateRoles(UserId id, Set<Role> roles) {
        UserEntity entity = repo.findById(id.value())
                .orElseThrow(() -> new UserNotFoundException(id));

        Set<Role> oldRoles = entity.getRoles();
        entity.setRoles(roles);
        repo.save(entity);

        log.info("Roles updated for user id={}: {} -> {}",
                id, oldRoles, roles.stream().map(Enum::name).collect(Collectors.toSet()));
    }
}
