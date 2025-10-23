// src/main/java/com/aredondocharro/ClothingStore/security/port/AuthPrincipal.java
package com.aredondocharro.ClothingStore.security.port;

import org.springframework.security.core.AuthenticatedPrincipal;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Principal autenticado mínimo.
 */
public record AuthPrincipal(
        String userId,
        List<String> authorities,
        Instant issuedAt,
        Instant expiresAt
) implements AuthenticatedPrincipal {

    public AuthPrincipal {
        // userId obligatorio y UUID válido
        Objects.requireNonNull(userId, "userId is required");
        try { UUID.fromString(userId); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("userId must be a UUID string"); }

        // authorities nunca nulo + inmutable
        authorities = (authorities == null) ? List.of() : List.copyOf(authorities);

        // coherencia temporal si ambos presentes
        if (issuedAt != null && expiresAt != null && !expiresAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException("expiresAt must be after issuedAt");
        }
    }

    /** Factory sin timestamps. */
    public static AuthPrincipal of(String userId, List<String> authorities) {
        return new AuthPrincipal(userId, authorities, null, null);
    }

    /** Requisito de AuthenticatedPrincipal: nombre del principal. */
    @Override
    public String getName() {
        return userId;
    }
}
