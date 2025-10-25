package com.aredondocharro.ClothingStore.identityTEST.infrastructure.out.persistence.mapper;

import com.aredondocharro.ClothingStore.identity.domain.model.PasswordResetTokenId;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordResetTokenRepositoryPort;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.PasswordResetTokenEntity;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.mapper.PasswordResetTokenMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetTokenMapperTest {

    @Test
    void toEntity_and_toToken_roundTrip() {
        PasswordResetTokenRepositoryPort.Token token =
                new PasswordResetTokenRepositoryPort.Token(
                        PasswordResetTokenId.of(UUID.randomUUID()),
                        UserId.of(UUID.randomUUID()),
                        "sha256hexhash",
                        Instant.now().plusSeconds(900),
                        null, // usedAt
                        Instant.now()
                );

        PasswordResetTokenEntity e = PasswordResetTokenMapper.toEntity(token);
        assertEquals(token.id().value(), e.getId());
        assertEquals(token.userId().value(), e.getUserId());
        assertEquals(token.tokenHash(), e.getTokenHash());
        assertEquals(token.expiresAt(), e.getExpiresAt());
        assertEquals(token.usedAt(), e.getUsedAt());
        assertEquals(token.createdAt(), e.getCreatedAt());

        PasswordResetTokenRepositoryPort.Token back = PasswordResetTokenMapper.toToken(e);
        assertEquals(token.id(), back.id());
        assertEquals(token.userId(), back.userId());
        assertEquals(token.tokenHash(), back.tokenHash());
        assertEquals(token.expiresAt(), back.expiresAt());
        assertEquals(token.usedAt(), back.usedAt());
        assertEquals(token.createdAt(), back.createdAt());
    }
}
