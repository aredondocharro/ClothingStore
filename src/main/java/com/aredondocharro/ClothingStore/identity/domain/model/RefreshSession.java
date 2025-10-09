package com.aredondocharro.ClothingStore.identity.domain.model;

import com.aredondocharro.ClothingStore.identity.domain.exception.RefreshSessionInvalidException;

import java.time.Instant;
import java.util.UUID;

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
    public RefreshSession {
        if (jti == null) throw RefreshSessionInvalidException.jtiRequired();
        if (userId == null) throw RefreshSessionInvalidException.userIdRequired();
        if (expiresAt == null) throw RefreshSessionInvalidException.expiresAtRequired();
        if (createdAt == null) createdAt = Instant.now();
        // revokedAt, replacedByJti, ip, userAgent pueden ser nulos inicialmente
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(Instant now) {
        return expiresAt.isBefore(now);
    }

    public boolean isRotated() {
        return replacedByJti != null;
    }

    public RefreshSession revoked(Instant when) {
        return new Builder()
                .jti(this.jti)
                .userId(this.userId)
                .expiresAt(this.expiresAt)
                .createdAt(this.createdAt)
                .revokedAt(when != null ? when : Instant.now())
                .replacedByJti(this.replacedByJti)
                .ip(this.ip)
                .userAgent(this.userAgent)
                .build();
    }

    public RefreshSession replacedBy(String newJti) {
        return new Builder()
                .jti(this.jti)
                .userId(this.userId)
                .expiresAt(this.expiresAt)
                .createdAt(this.createdAt)
                .revokedAt(this.revokedAt)
                .replacedByJti(newJti)
                .ip(this.ip)
                .userAgent(this.userAgent)
                .build();
    }

    public static RefreshSession issued(String jti, UUID userId, Instant expiresAt, String ip, String userAgent) {
        return new Builder()
                .jti(jti)
                .userId(userId)
                .expiresAt(expiresAt)
                .ip(ip)
                .userAgent(userAgent)
                .build(); // createdAt se rellena en el constructor si viene null
    }

    public static class Builder {
        private String jti;
        private UUID userId;
        private Instant expiresAt;
        private Instant createdAt;
        private Instant revokedAt;
        private String replacedByJti;
        private String ip;
        private String userAgent;

        public Builder jti(String jti) {
            this.jti = jti;
            return this;
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder revokedAt(Instant revokedAt) {
            this.revokedAt = revokedAt;
            return this;
        }

        public Builder replacedByJti(String replacedByJti) {
            this.replacedByJti = replacedByJti;
            return this;
        }

        public Builder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public RefreshSession build() {
            return new RefreshSession(jti, userId, expiresAt, createdAt, revokedAt, replacedByJti, ip, userAgent);
        }
    }


}