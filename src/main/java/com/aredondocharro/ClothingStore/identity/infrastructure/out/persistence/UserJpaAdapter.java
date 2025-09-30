package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence;

import com.aredondocharro.ClothingStore.identity.domain.model.Email;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordHash;
import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.port.out.LoadUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SaveUserPort;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.UserEntity;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class UserJpaAdapter implements LoadUserPort, SaveUserPort {

    private final SpringDataUserRepository repo;

    public Optional<User> findByEmail(Email email) {
        log.debug("Finding user by email={}", email.getValue());
        return repo.findByEmail(email.getValue()).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        log.debug("Finding user by id={}", id);
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        // Evita loggear datos sensibles (hash). Email está bien.
        log.debug("Saving user email={}", user.email().getValue());
        UserEntity saved = repo.save(toEntity(user));
        log.info("User persisted id={} email={}", saved.getId(), saved.getEmail());
        return toDomain(saved);
    }

    /* ===================== MAPPING ===================== */

    private User toDomain(UserEntity e) {
        return new User(
                e.getId(),
                Email.of(e.getEmail()),
                PasswordHash.ofHashed(e.getPasswordHash()),
                e.isEmailVerified(),
                e.getRoles().stream()
                        .map(Role::from)
                        .collect(Collectors.toUnmodifiableSet()),
                e.getCreatedAt()
        );
    }

    private UserEntity toEntity(User u) {
        return UserEntity.builder()
                // idealmente el dominio ya trae id != null; si no, puedes mantener el fallback:
                .id(u.id() == null ? UUID.randomUUID() : u.id())
                .email(u.email().getValue())
                .passwordHash(u.passwordHash().getValue())
                .emailVerified(u.emailVerified())
                .roles(u.roles().stream().map(Role::name).collect(Collectors.toSet()))
                .createdAt(u.createdAt())
                .build();
    }
}
