package com.aredondocharro.ClothingStore.identity.domain.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class User {
    UUID id;
    String email;
    String passwordHash;
    boolean emailVerified;
    @Singular Set<String> roles;
    Instant createdAt;

    public User(UUID id, String email, String passwordHash, boolean emailVerified, Set<String> roles, Instant createdAt) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("email is required");
        if (passwordHash == null || passwordHash.isBlank()) throw new IllegalArgumentException("passwordHash is required");
        this.id = id == null ? UUID.randomUUID() : id;
        this.email = email.toLowerCase();
        this.passwordHash = passwordHash;
        this.emailVerified = emailVerified;
        this.roles = roles == null || roles.isEmpty() ? Set.of("USER") : Set.copyOf(roles);
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public User verified() { return this.toBuilder().emailVerified(true).build(); }
}
