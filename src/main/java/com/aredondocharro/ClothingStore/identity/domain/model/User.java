package com.aredondocharro.ClothingStore.identity.domain.model;

import com.aredondocharro.ClothingStore.identity.domain.exception.EmailRequiredException;
import com.aredondocharro.ClothingStore.identity.domain.exception.HashedPasswordRequiredException;
import lombok.Builder;
import lombok.Singular;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;


@Builder(toBuilder = true)
public record User(UUID id, Email email, PasswordHash passwordHash, boolean emailVerified, @Singular Set<Role> roles,
                   Instant createdAt) {
    public User(UUID id, Email email, PasswordHash passwordHash, boolean emailVerified,
                Set<Role> roles, Instant createdAt) {
        if (email == null) throw new EmailRequiredException();
        if (passwordHash == null) throw new HashedPasswordRequiredException();

        this.id = (id == null) ? UUID.randomUUID() : id;
        this.email = email; // Email ya normaliza/valida
        this.passwordHash = passwordHash;
        this.emailVerified = emailVerified;
        this.roles = (roles == null || roles.isEmpty()) ? Set.of(Role.USER) : Set.copyOf(roles);
        this.createdAt = (createdAt == null) ? Instant.now() : createdAt;
    }

    public User verified() {
        return this.toBuilder().emailVerified(true).build();
    }
}
