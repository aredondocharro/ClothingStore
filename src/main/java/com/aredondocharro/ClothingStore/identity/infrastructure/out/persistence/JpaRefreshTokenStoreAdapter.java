package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence;

import com.aredondocharro.ClothingStore.identity.domain.model.RefreshSession;
import com.aredondocharro.ClothingStore.identity.domain.port.out.RefreshTokenStorePort;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.RefreshSessionEntity;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringDataRefreshSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class JpaRefreshTokenStoreAdapter implements RefreshTokenStorePort {

    private final SpringDataRefreshSessionRepository repo;

    @Override
    public Optional<RefreshSession> findByJti(String jti) {
        return repo.findByJti(jti).map(this::toDomain);
    }

    @Override
    @Transactional
    public RefreshSession saveNew(RefreshSession session, String rawRefreshToken) {
        RefreshSessionEntity entity = toEntity(session);
        entity.setTokenHash(sha256(rawRefreshToken));
        RefreshSessionEntity saved = repo.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional
    public void markReplaced(String oldJti, String newJti, Instant when) {
        RefreshSessionEntity e = repo.findById(oldJti).orElse(null);
        if (e == null) return;
        e.setRevokedAt(when);
        e.setReplacedByJti(newJti);
        repo.save(e);
    }

    @Override
    @Transactional
    public void revoke(String jti, String reason, Instant when) {
        RefreshSessionEntity e = repo.findById(jti).orElse(null);
        if (e == null) return;
        e.setRevokedAt(when);
        repo.save(e);
        log.info("Refresh revoked jti={} reason={}", jti, reason);
    }

    @Override
    @Transactional
    public void revokeAllForUser(UUID userId, String reason, Instant when) {
        // naive approach; si el volumen es grande, crear query custom
        repo.findAll().stream()
                .filter(e -> userId.equals(e.getUserId()))
                .filter(e -> e.getRevokedAt() == null)
                .forEach(e -> { e.setRevokedAt(when); repo.save(e); });
        log.warn("All refresh sessions revoked for userId={} reason={}", userId, reason);
    }

    private RefreshSessionEntity toEntity(RefreshSession s) {
        return RefreshSessionEntity.builder()
                .jti(s.jti())
                .userId(s.userId())
                .expiresAt(s.expiresAt())
                .createdAt(s.createdAt())
                .revokedAt(s.revokedAt())
                .replacedByJti(s.replacedByJti())
                .ip(s.ip())
                .userAgent(s.userAgent())
                .tokenHash("unset")
                .build();
    }

    private RefreshSession toDomain(RefreshSessionEntity e) {
        return new RefreshSession(
                e.getJti(),
                e.getUserId(),
                e.getExpiresAt(),
                e.getCreatedAt(),
                e.getRevokedAt(),
                e.getReplacedByJti(),
                e.getIp(),
                e.getUserAgent()
        );
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
