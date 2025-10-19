package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA/Hibernate
@AllArgsConstructor
@Builder
@Entity
@Table(name = "password_reset_tokens",
        indexes = {
                @Index(name = "idx_prt_token_hash", columnList = "token_hash"),
                @Index(name = "idx_prt_user_expires", columnList = "user_id,expires_at")
        })
public class PasswordResetTokenEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, length = 128)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
