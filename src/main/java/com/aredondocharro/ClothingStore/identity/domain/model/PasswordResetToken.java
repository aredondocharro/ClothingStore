package com.aredondocharro.ClothingStore.identity.domain.model;

import java.time.Instant;

public record PasswordResetToken(
        PasswordResetTokenId id,
        UserId userId,
        String tokenHash,
        Instant expiresAt,
        Instant usedAt,
        Instant createdAt
) {
    public boolean isExpiredAt(Instant now) { return now.isAfter(expiresAt); }
    public boolean isUsed() { return usedAt != null; }

    public PasswordResetToken markUsed(Instant when) {
        return new PasswordResetToken(id, userId, tokenHash, expiresAt, when, createdAt);
    }
}
