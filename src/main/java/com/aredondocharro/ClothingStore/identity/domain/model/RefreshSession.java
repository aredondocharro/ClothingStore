package com.aredondocharro.ClothingStore.identity.domain.model;

import com.aredondocharro.ClothingStore.identity.domain.exception.RefreshSessionInvalidException;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder(toBuilder = true)
public record RefreshSession(
        String jti,
        UUID userId,
        Instant expiresAt,
        Instant createdAt,
        Instant revokedAt,
        String replacedByJti,
        String ip,
        String userAgent
) {
    // Compact constructor: valida invariantes y pone defaults
    public RefreshSession {
        if (jti == null) throw RefreshSessionInvalidException.jtiRequired();
        if (userId == null) throw RefreshSessionInvalidException.userIdRequired();
        if (expiresAt == null) throw RefreshSessionInvalidException.expiresAtRequired();
        if (createdAt == null) createdAt = Instant.now();
        // revokedAt, replacedByJti, ip, userAgent pueden ser nulos inicialmente
    }

    public boolean isRevoked() { return revokedAt != null; }
    public boolean isExpired(Instant now) { return expiresAt.isBefore(now); }
    public boolean isRotated() { return replacedByJti != null; }

    // Métodos de conveniencia inmutables
    public RefreshSession revoked(Instant when) {
        return this.toBuilder().revokedAt(when != null ? when : Instant.now()).build();
    }
    public RefreshSession replacedBy(String newJti) {
        return this.toBuilder().replacedByJti(newJti).build();
    }

    // Factory para una sesión recién emitida (cero nulls explícitos al llamarlo)
    public static RefreshSession issued(String jti, UUID userId, Instant expiresAt, String ip, String userAgent) {
        return RefreshSession.builder()
                .jti(jti)
                .userId(userId)
                .expiresAt(expiresAt)
                .ip(ip)
                .userAgent(userAgent)
                .build(); // createdAt se rellena en el constructor si viene null
    }
}
