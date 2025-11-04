package com.aredondocharro.ClothingStore.identity.domain.port.out;

import com.aredondocharro.ClothingStore.identity.domain.model.PasswordResetToken;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordResetTokenId;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepositoryPort {
    void save(PasswordResetToken token);
    Optional<PasswordResetToken> findValidByHash(String tokenHash, Instant now);
    void markUsed(PasswordResetTokenId id, Instant usedAt);
    void deleteAllForUser(UserId userId);
}