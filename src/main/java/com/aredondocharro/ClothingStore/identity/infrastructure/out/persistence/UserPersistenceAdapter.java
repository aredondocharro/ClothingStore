package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence;

import com.aredondocharro.ClothingStore.identity.domain.model.*;
import com.aredondocharro.ClothingStore.identity.domain.port.out.LoadUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SaveUserPort;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.UserEntity;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringDataUserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static com.aredondocharro.ClothingStore.shared.log.LogSanitizer.maskEmail;

@Slf4j
@RequiredArgsConstructor
public class UserPersistenceAdapter implements LoadUserPort, SaveUserPort {

    private final SpringDataUserRepository repo;

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(IdentityEmail email) {
        log.debug("Finding user by email={}", email.getValue());
        return repo.findByEmailIgnoreCase(email.getValue()).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UserId id) {
        log.debug("Finding user by id={}", id.value());
        return repo.findById(id.value()).map(this::toDomain);
    }

    @Override
    @Transactional
    public User save(User user) {
        if (user.id() == null) {
            throw new IllegalArgumentException("User.id must be provided by application layer");
        }
        if (user.createdAt() == null) {
            throw new IllegalArgumentException("User.createdAt must be provided by application layer");
        }

        log.debug("Saving user email={}", user.email().getValue());
        UserEntity saved = repo.save(toEntity(user));
        log.info("User persisted id={}", saved.getId());
        log.debug("User persisted id={} email={}", saved.getId(), maskEmail(saved.getEmail()));
        return toDomain(saved);
    }

    private User toDomain(UserEntity e) {
        Set<Role> roles = (e.getRoles() == null || e.getRoles().isEmpty())
                ? Set.of(Role.USER)
                : Set.copyOf(e.getRoles());

        return new User(
                UserId.of(e.getId()),
                IdentityEmail.of(e.getEmail()),
                PasswordHash.ofHashed(e.getPasswordHash()),
                e.isEmailVerified(),
                roles,
                e.getCreatedAt()
        );
    }

    private UserEntity toEntity(User u) {
        Set<Role> roles = (u.roles() == null || u.roles().isEmpty())
                ? Set.of(Role.USER)
                : Set.copyOf(u.roles());

        return UserEntity.builder()
                .id(u.id().value()) // UUID desde app
                .email(u.email().getValue())
                .passwordHash(u.passwordHash().getValue())
                .emailVerified(u.emailVerified())
                .roles(roles)
                .createdAt(u.createdAt())
                .build();
    }
}

