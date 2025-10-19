package com.aredondocharro.ClothingStore.identity.domain.model;

import com.aredondocharro.ClothingStore.identity.domain.exception.RefreshSessionInvalidException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record RefreshSession(
        String jti,
        UserId userId,
        Instant expiresAt,
        Instant createdAt,
        Instant revokedAt,
        String replacedByJti,
        String ip,
        String userAgent
) {
    public RefreshSession {
        if (jti == null || jti.isBlank()) throw RefreshSessionInvalidException.jtiRequired();
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(expiresAt, "expiresAt");
        Objects.requireNonNull(createdAt, "createdAt");
        // Asegura orden temporal coherente
        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("expiresAt must be after createdAt");
        }
        // revokedAt, replacedByJti, ip, userAgent pueden ser nulos
    }

    /** True si está revocada (independiente de expiración). */
    public boolean isRevoked() {
        return revokedAt != null;
    }

    /** True si ya está expirada respecto a 'now' (inclusivo: now >= exp ⇒ expirada). */
    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(Objects.requireNonNull(now));
    }

    /** Activa = no revocada y no expirada. */
    public boolean isActive(Instant now) {
        return !isRevoked() && !isExpired(now);
    }

    /** Devuelve una copia marcada como revocada (idempotente). */
    public RefreshSession revoke(Instant when) {
        Objects.requireNonNull(when, "when");
        if (this.revokedAt != null) return this;
        return new RefreshSession(jti, userId, expiresAt, createdAt, when, replacedByJti, ip, userAgent);
    }

    /** Marca que esta sesión fue rotada por otra (nuevo jti). */
    public RefreshSession rotatedTo(String newJti) {
        if (newJti == null || newJti.isBlank()) {
            throw RefreshSessionInvalidException.replacedByRequired();
        }
        if (this.revokedAt != null) {
            throw new IllegalStateException("Cannot rotate a revoked session");
        }
        return new RefreshSession(jti, userId, expiresAt, createdAt, revokedAt, newJti, ip, userAgent);
    }

    /* ===================== FÁBRICAS ===================== */

    /**
     * Crear una nueva sesión (emitida ahora/BAJO CONTROL de aplicación).
     * Mantiene el orden (createdAt, expiresAt) para no romper llamadas existentes.
     */
    public static RefreshSession issue(String jti,
                                       UserId userId,
                                       Instant createdAt,
                                       Instant expiresAt,
                                       String ip,
                                       String userAgent) {
        return new RefreshSession(jti, userId, expiresAt, createdAt, null, null, ip, userAgent);
    }

    /**
     * Crear desde un JWT decodificado. Si el token no trae 'iat', usamos 'now'.
     */
    public static RefreshSession fromDecoded(String jti,
                                             UserId userId,
                                             Instant decodedIat,
                                             Instant decodedExp,
                                             Instant now,
                                             String ip,
                                             String userAgent) {
        Instant created = (decodedIat != null) ? decodedIat : Objects.requireNonNull(now, "now");
        return issue(jti, userId, created, Objects.requireNonNull(decodedExp, "decodedExp"), ip, userAgent);
    }

    /** Reconstrucción desde persistencia/adapters con VO de usuario. */
    public static RefreshSession rehydrate(String jti,
                                           UserId userId,
                                           Instant expiresAt,
                                           Instant createdAt,
                                           Instant revokedAt,
                                           String replacedByJti,
                                           String ip,
                                           String userAgent) {
        return new RefreshSession(jti, userId, expiresAt, createdAt, revokedAt, replacedByJti, ip, userAgent);
    }

    /** Conveniencia para adapters que todavía reciben UUID crudo. */
    public static RefreshSession rehydrate(String jti,
                                           UUID userId,
                                           Instant expiresAt,
                                           Instant createdAt,
                                           Instant revokedAt,
                                           String replacedByJti,
                                           String ip,
                                           String userAgent) {
        return rehydrate(jti, UserId.of(userId), expiresAt, createdAt, revokedAt, replacedByJti, ip, userAgent);
    }
}
