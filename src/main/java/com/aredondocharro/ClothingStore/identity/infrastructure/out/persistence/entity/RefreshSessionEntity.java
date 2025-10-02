package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_session")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshSessionEntity {

    @Id
    @Column(length = 36)
    private String jti;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant revokedAt;

    @Column(length = 36)
    private String replacedByJti;

    @Column(length = 64, nullable = false)
    private String tokenHash; // SHA-256 hex

    @Column(length = 64)
    private String ip;

    @Column(length = 256)
    private String userAgent;
}
