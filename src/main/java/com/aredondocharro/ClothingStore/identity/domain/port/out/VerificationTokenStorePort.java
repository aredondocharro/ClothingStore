package com.aredondocharro.ClothingStore.identity.domain.port.out;

import com.aredondocharro.ClothingStore.identity.domain.model.UserId;

import java.time.Instant;
import java.util.UUID;

public interface VerificationTokenStorePort {
    void revokeActiveTokensForUser(UserId userId, Instant revokedAt);
    void saveNewToken(UserId userId, UUID jti, Instant expiresAt);
    boolean isTokenActive(UUID jti);
    void revokeToken(UUID jti, Instant revokedAt);
}

