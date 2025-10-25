package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence;

import com.aredondocharro.ClothingStore.identity.domain.model.PasswordResetTokenId;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordResetTokenRepositoryPort;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.PasswordResetTokenEntity;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.mapper.PasswordResetTokenMapper;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringPasswordResetTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepositoryPort {

    private final SpringPasswordResetTokenJpaRepository jpa;

    @Override
    @Transactional
    public void save(Token token) {
        PasswordResetTokenEntity e = PasswordResetTokenMapper.toEntity(token);
        jpa.save(e);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Token> findValidByHash(String tokenHash, Instant now) {
        return jpa.findByTokenHashAndExpiresAtAfterAndUsedAtIsNull(tokenHash, now)
                .map(e -> new Token(
                        PasswordResetTokenId.of(e.getId()), // UUID -> VO
                        UserId.of(e.getUserId()),           // UUID -> VO
                        e.getTokenHash(),
                        e.getExpiresAt(),
                        e.getUsedAt(),
                        e.getCreatedAt()
                ));
    }

    @Override
    @Transactional
    public void markUsed(PasswordResetTokenId id, Instant usedAt) {
        var e = jpa.findById(id.value()).orElseThrow();
        e.setUsedAt(usedAt);
        jpa.save(e);
    }

    @Override
    @Transactional
    public void deleteAllForUser(UserId userId) {
        jpa.deleteAllByUserId(userId.value());
    }
}
