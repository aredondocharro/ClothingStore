package com.aredondocharro.ClothingStore.identity.domain.model;

import com.aredondocharro.ClothingStore.identity.domain.exception.EmailRequiredException;
import com.aredondocharro.ClothingStore.identity.domain.exception.HashedPasswordRequiredException;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public record User(
        UserId id,
        IdentityEmail email,
        PasswordHash passwordHash,
        boolean emailVerified,
        Set<Role> roles,
        Instant createdAt
) {
    public User {
        if (email == null) throw new EmailRequiredException();
        if (passwordHash == null) throw new HashedPasswordRequiredException();
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(createdAt, "createdAt");

        // Normaliza roles (inmutable y con USER por defecto)
        roles = (roles == null || roles.isEmpty()) ? Set.of(Role.USER) : Set.copyOf(roles);
    }

    /** Marca el email como verificado (devuelve nueva instancia) */
    public User verified() {
        return emailVerified ? this : new User(id, email, passwordHash, true, roles, createdAt);
    }

    /** Fábrica para altas nuevas (emailVerified=false) */
    public static User create(UserId id, IdentityEmail email, PasswordHash hash, Set<Role> roles, Instant createdAt) {
        return new User(id, email, hash, false, roles, createdAt);
    }

    /** Reconstrucción desde persistencia/mapeo */
    public static User rehydrate(UserId id, IdentityEmail email, PasswordHash hash, boolean emailVerified,
                                 Set<Role> roles, Instant createdAt) {
        return new User(id, email, hash, emailVerified, roles, createdAt);
    }

    /** Conveniencia si tu adapter aún tiene UUID crudo */
    public static User rehydrate(java.util.UUID id, IdentityEmail email, PasswordHash hash, boolean emailVerified,
                                 Set<Role> roles, Instant createdAt) {
        return rehydrate(UserId.of(id), email, hash, emailVerified, roles, createdAt);
    }
}
