package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA exige no-args
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "refresh_session",
        indexes = {
                @Index(name = "idx_refresh_token_hash", columnList = "token_hash")
                // añade los que uses realmente en queries (p.ej. user_id, revoked_at, etc.)
        }
)
public class RefreshSessionEntity {

    @Id
    @Column(name = "jti", length = 64, nullable = false) // 64 si JTI es UUID/hex
    private String jti;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by_jti", length = 64)
    private String replacedByJti;

    // Deja 128 si en tu esquema/migración ya está a 128 para no desalinear
    @Column(name = "token_hash", length = 128, nullable = false)
    private String tokenHash;  // SHA-256 hex: 64, pero 128 es “sobrado” y no rompe

    @Column(name = "ip", length = 64)
    private String ip; // suficiente para IPv6

    @Column(name = "user_agent", length = 512)
    private String userAgent; // 256 a veces se queda corto
}
