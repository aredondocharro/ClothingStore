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

import static com.aredondocharro.ClothingStore.shared.log.LogSanitizer.maskEmail;

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
        log.debug("Saving user email={}", user.email().getValue());
        UserEntity saved = repo.save(toEntity(user));
        log.info("User persisted id={}", saved.getId());
        log.debug("User persisted id={} email={}", saved.getId(), maskEmail(saved.getEmail()));
        return toDomain(saved);
    }

    /* ===================== MAPPING ===================== */

    private User toDomain(UserEntity e) {
        var roleStrings = (e.getRoles() == null) ? java.util.Set.<String>of() : e.getRoles();
        var roles = roleStrings.stream().map(Role::from).collect(java.util.stream.Collectors.toUnmodifiableSet());
        return new User(
                e.getId(),
                Email.of(e.getEmail()),
                PasswordHash.ofHashed(e.getPasswordHash()),
                e.isEmailVerified(),
                roles,
                e.getCreatedAt()
        );
    }

    private UserEntity toEntity(User u) {
        return UserEntity.builder()
                .id(u.id()) // @PrePersist lo rellena si es null
                .email(u.email().getValue())
                .passwordHash(u.passwordHash().getValue())
                .emailVerified(u.emailVerified())
                .roles(u.roles().stream().map(Role::name).collect(java.util.stream.Collectors.toSet()))
                .createdAt(u.createdAt()) // @PrePersist si es null
                .build();
    }
}
