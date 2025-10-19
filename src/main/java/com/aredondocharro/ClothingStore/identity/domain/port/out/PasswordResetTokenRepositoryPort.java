package com.aredondocharro.ClothingStore.identity.domain.port.out;

import com.aredondocharro.ClothingStore.identity.domain.model.PasswordResetTokenId;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepositoryPort {

    void save(Token token);

    Optional<Token> findValidByHash(String tokenHash, Instant now);

    void markUsed(PasswordResetTokenId id, Instant usedAt);

    void deleteAllForUser(UserId userId);

    final class Token {
        private final PasswordResetTokenId id;
        private final UserId userId;
        private final String tokenHash;
        private final Instant expiresAt;
        private final Instant usedAt;
        private final Instant createdAt;

        public Token(PasswordResetTokenId id, UserId userId, String tokenHash, Instant expiresAt, Instant usedAt, Instant createdAt) {
            this.id = id;
            this.userId = userId;
            this.tokenHash = tokenHash;
            this.expiresAt = expiresAt;
            this.usedAt = usedAt;
            this.createdAt = createdAt;
        }

        public PasswordResetTokenId id() { return id; }
        public UserId userId() { return userId; }
        public String tokenHash() { return tokenHash; }
        public Instant expiresAt() { return expiresAt; }
        public Instant usedAt() { return usedAt; }
        public Instant createdAt() { return createdAt; }
    }
}
