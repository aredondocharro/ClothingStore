package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence;

import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordHash;
import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.port.out.LoadUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SaveUserPort;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.UserEntity;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringDataUserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.aredondocharro.ClothingStore.shared.log.LogSanitizer.maskEmail;

@Slf4j
@AllArgsConstructor
public class UserPersistenceAdapter implements LoadUserPort, SaveUserPort {

    private final SpringDataUserRepository repo;

    // ---------------------------------------------------------------------
    // API adicional usado por tests / casos de uso (no necesariamente @Override)
    // ---------------------------------------------------------------------
    public Optional<User> findByEmail(IdentityEmail email) {
        log.debug("Finding user by email={}", email.getValue());
        return repo.findByEmailIgnoreCase(email.getValue()).map(this::toDomain);
    }

    // ---------------------------------------------------------------------
    // LoadUserPort
    // ---------------------------------------------------------------------
    @Override
    public Optional<User> findById(UUID id) {
        log.debug("Finding user by id={}", id);
        return repo.findById(id).map(this::toDomain);
    }

    // ---------------------------------------------------------------------
    // SaveUserPort
    // ---------------------------------------------------------------------
    @Override
    @Transactional
    public User save(User user) {
        log.debug("Saving user email={}", user.email().getValue());
        UserEntity saved = repo.save(toEntity(user));
        log.info("User persisted id={}", saved.getId());
        log.debug("User persisted id={} email={}", saved.getId(), maskEmail(saved.getEmail()));
        return toDomain(saved);
    }

    /* ===================== MAPPING ===================== */

    private User toDomain(UserEntity e) {
        // Si BD trae null o vacío, por defecto Role.USER
        Set<Role> roles = (e.getRoles() == null || e.getRoles().isEmpty())
                ? Set.of(Role.USER)
                : Set.copyOf(e.getRoles());  // ✅ Ya es Set<Role>, solo copia inmutable

        return new User(
                e.getId(),
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
                : u.roles();

        UUID id = (u.id() != null) ? u.id() : UUID.randomUUID();
        Instant createdAt = (u.createdAt() != null) ? u.createdAt() : Instant.now();

        return UserEntity.builder()
                .id(id)
                .email(u.email().getValue())
                .passwordHash(u.passwordHash().getValue())
                .emailVerified(u.emailVerified())
                .roles(roles)
                .createdAt(createdAt)
                .build();
    }
}
