package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence;

import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.out.VerificationTokenStorePort;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.VerificationTokenEntity;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Transactional
public class VerificationTokenStoreJpaAdapter implements VerificationTokenStorePort {

    private final VerificationTokenRepository repo;
    private final Clock clock;

    @Override
    public void saveNewToken(UserId userId, UUID jti, Instant expiresAt) {
        Instant issuedAt = Instant.now(clock);

        log.info("[verification-store] Saving new token userId={} jti={} issuedAt={} expiresAt={}",
                userId.value(), jti, issuedAt, expiresAt);

        VerificationTokenEntity entity = VerificationTokenEntity.builder()
                .userId(userId.value())
                .jti(jti)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();

        repo.saveAndFlush(entity); // 👈 forzamos insert inmediato

        log.info("[verification-store] Saved token row id={} for userId={} jti={}",
                entity.getId(), userId.value(), jti);
    }

    @Override
    public void revokeActiveTokensForUser(UserId userId, Instant revokedAt) {
        log.info("[verification-store] Revoking active tokens for userId={} revokedAt={}",
                userId.value(), revokedAt);

        List<VerificationTokenEntity> active =
                repo.findByUserIdAndRevokedAtIsNull(userId.value());

        active.forEach(e -> e.setRevokedAt(revokedAt));
        repo.saveAll(active);

        log.info("[verification-store] Revoked {} active tokens for userId={}",
                active.size(), userId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTokenActive(UUID jti) {
        boolean active = repo.findByJti(jti)
                .filter(e -> e.getRevokedAt() == null)
                .isPresent();

        log.info("[verification-store] isTokenActive jti={} -> {}", jti, active);
        return active;
    }

    @Override
    public void revokeToken(UUID jti, Instant revokedAt) {
        log.info("[verification-store] Revoking token jti={} revokedAt={}", jti, revokedAt);
        int updated = repo.markAsRevokedByJti(jti, revokedAt);
        log.info("[verification-store] revokeToken jti={} updatedRows={}", jti, updated);
    }
}
