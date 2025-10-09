package com.aredondocharro.ClothingStore.identity.domain.model;

import com.aredondocharro.ClothingStore.identity.domain.exception.EmailRequiredException;
import com.aredondocharro.ClothingStore.identity.domain.exception.HashedPasswordRequiredException;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record User(UUID id, IdentityEmail email, PasswordHash passwordHash, boolean emailVerified, Set<Role> roles,
                   Instant createdAt) {

    public User(UUID id, IdentityEmail email, PasswordHash passwordHash, boolean emailVerified,
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
        return new User(this.id, this.email, this.passwordHash, true, this.roles, this.createdAt);
    }

    public static class Builder {
        private UUID id;
        private IdentityEmail email;
        private PasswordHash passwordHash;
        private boolean emailVerified;
        private Set<Role> roles = new HashSet<>();
        private Instant createdAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder email(IdentityEmail email) {
            this.email = email;
            return this;
        }

        public Builder passwordHash(PasswordHash passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public Builder emailVerified(boolean emailVerified) {
            this.emailVerified = emailVerified;
            return this;
        }

        public Builder role(Role role) {
            this.roles.add(role);
            return this;
        }

        public Builder roles(Set<Role> roles) {
            if (roles != null) {
                this.roles.addAll(roles);
            }
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public User build() {
            return new User(id, email, passwordHash, emailVerified, Collections.unmodifiableSet(roles), createdAt);
        }
    }

    public Builder toBuilder() {
        Builder builder = new Builder();
        builder.id = this.id;
        builder.email = this.email;
        builder.passwordHash = this.passwordHash;
        builder.emailVerified = this.emailVerified;
        builder.roles.addAll(this.roles);
        builder.createdAt = this.createdAt;
        return builder;
    }

    public static Builder builder() {
        return new Builder();
    }
}
