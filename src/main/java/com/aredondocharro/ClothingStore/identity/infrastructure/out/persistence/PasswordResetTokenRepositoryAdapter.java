package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence;

import com.aredondocharro.ClothingStore.identity.domain.model.PasswordResetToken;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordResetTokenId;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordResetTokenRepositoryPort;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.mapper.PasswordResetTokenMapper;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringPasswordResetTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepositoryPort {

    private final SpringPasswordResetTokenJpaRepository jpa;
    private final PasswordResetTokenMapper mapper;

    @Override
    public void save(PasswordResetToken token) {
        jpa.save(mapper.toEntity(token));
    }

    @Override
    public Optional<PasswordResetToken> findValidByHash(String tokenHash, Instant now) {
        return jpa.findByTokenHashAndExpiresAtAfterAndUsedAtIsNull(tokenHash, now)
                .map(mapper::toDomain);
    }

    @Override
    public void markUsed(PasswordResetTokenId id, Instant usedAt) {
        var e = jpa.findById(id.value()).orElseThrow();
        e.setUsedAt(usedAt);
        jpa.save(e);
    }

    @Override
    public void deleteAllForUser(UserId userId) {
        jpa.deleteAllByUserId(userId.value());
    }
}

