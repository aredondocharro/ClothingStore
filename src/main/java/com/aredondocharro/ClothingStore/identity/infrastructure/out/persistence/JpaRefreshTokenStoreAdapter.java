package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence;

import com.aredondocharro.ClothingStore.identity.domain.model.RefreshSession;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.out.RefreshTokenStorePort;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.RefreshSessionEntity;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringDataRefreshSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY) // ← exige una TX abierta por el wrapper
public class JpaRefreshTokenStoreAdapter implements RefreshTokenStorePort {

    private final SpringDataRefreshSessionRepository repo;

    @Override
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY) // ← query
    public Optional<RefreshSession> findByJti(String jti) {
        return repo.findByJti(jti).map(this::toDomain);
    }

    @Override
    public RefreshSession saveNew(RefreshSession session, String rawRefreshToken) {
        RefreshSessionEntity entity = toEntity(session);
        entity.setTokenHash(sha256(rawRefreshToken)); // hex 64 chars
        RefreshSessionEntity saved = repo.save(entity);
        return toDomain(saved);
    }

    @Override
    public void markReplaced(String oldJti, String newJti, Instant when) {
        repo.findById(oldJti).ifPresent(e -> {
            e.setRevokedAt(when);
            e.setReplacedByJti(newJti);
            repo.save(e);
        });
    }

    @Override
    public void revoke(String jti, String reason, Instant when) {
        repo.findById(jti).ifPresent(e -> {
            if (e.getRevokedAt() == null) {
                e.setRevokedAt(when);
                repo.save(e);
                log.info("Refresh revoked jti={} reason={}", jti, reason);
            }
        });
    }

    @Override
    public void revokeAllForUser(UserId userId, String reason, Instant when) {
        int n = repo.revokeAllForUser(userId.value(), when);
        log.warn("All refresh sessions revoked for userId={} reason={} updated={}", userId, reason, n);
    }

    /* ===================== mapping ===================== */

    private RefreshSessionEntity toEntity(RefreshSession s) {
        return RefreshSessionEntity.builder()
                .jti(s.jti())
                .userId(s.userId().value())
                .expiresAt(s.expiresAt())
                .createdAt(s.createdAt())
                .revokedAt(s.revokedAt())
                .replacedByJti(s.replacedByJti())
                .ip(s.ip())
                .userAgent(s.userAgent())
                .tokenHash("unset") // se setea arriba con el hash real
                .build();
    }

    private RefreshSession toDomain(RefreshSessionEntity e) {
        return new RefreshSession(
                e.getJti(),
                UserId.of(e.getUserId()),
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
            return HexFormat.of().formatHex(bytes); // 64 chars hex
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
