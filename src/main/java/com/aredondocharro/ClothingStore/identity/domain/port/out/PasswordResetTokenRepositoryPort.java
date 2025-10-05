package com.aredondocharro.ClothingStore.identity.domain.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepositoryPort {

    void save(Token token);

    Optional<Token> findValidByHash(String tokenHash, Instant now);

    void markUsed(UUID id, Instant usedAt);

    void deleteAllForUser(UUID userId);

    final class Token {
        private final UUID id;
        private final UUID userId;
        private final String tokenHash;
        private final Instant expiresAt;
        private final Instant usedAt;
        private final Instant createdAt;

        public Token(UUID id, UUID userId, String tokenHash, Instant expiresAt, Instant usedAt, Instant createdAt) {
            this.id = id;
            this.userId = userId;
            this.tokenHash = tokenHash;
            this.expiresAt = expiresAt;
            this.usedAt = usedAt;
            this.createdAt = createdAt;
        }

        public UUID id() { return id; }
        public UUID userId() { return userId; }
        public String tokenHash() { return tokenHash; }
        public Instant expiresAt() { return expiresAt; }
        public Instant usedAt() { return usedAt; }
        public Instant createdAt() { return createdAt; }
    }
}
