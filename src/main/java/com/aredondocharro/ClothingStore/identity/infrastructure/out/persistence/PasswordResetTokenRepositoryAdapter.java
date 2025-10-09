package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence;


import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordResetTokenRepositoryPort;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.PasswordResetTokenEntity;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringPasswordResetTokenJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepositoryPort {

    private final SpringPasswordResetTokenJpaRepository jpa;

    public PasswordResetTokenRepositoryAdapter(SpringPasswordResetTokenJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public void save(Token token) {
        PasswordResetTokenEntity e = new PasswordResetTokenEntity();
        e.setId(token.id());
        e.setUserId(token.userId());
        e.setTokenHash(token.tokenHash());
        e.setExpiresAt(token.expiresAt());
        e.setUsedAt(token.usedAt());
        e.setCreatedAt(token.createdAt());
        jpa.save(e);
    }

    @Override
    public Optional<Token> findValidByHash(String tokenHash, Instant now) {
        return jpa.findByTokenHashAndExpiresAtAfterAndUsedAtIsNull(tokenHash, now)
                .map(e -> new Token(e.getId(), e.getUserId(), e.getTokenHash(),
                        e.getExpiresAt(), e.getUsedAt(), e.getCreatedAt()));
    }

    @Override
    @Transactional
    public void markUsed(UUID id, Instant usedAt) {
        PasswordResetTokenEntity e = jpa.findById(id).orElseThrow();
        e.setUsedAt(usedAt);
        jpa.save(e);
    }

    @Override
    @Transactional
    public void deleteAllForUser(UUID userId) {
        jpa.deleteAllByUserId(userId);
    }
}
