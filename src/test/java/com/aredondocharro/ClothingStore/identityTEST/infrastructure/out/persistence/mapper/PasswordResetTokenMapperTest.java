package com.aredondocharro.ClothingStore.identityTEST.infrastructure.out.persistence.mapper;

import com.aredondocharro.ClothingStore.identity.domain.model.PasswordResetToken;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordResetTokenId;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.PasswordResetTokenEntity;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.mapper.PasswordResetTokenMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetTokenMapperTest {

    private final PasswordResetTokenMapper mapper = Mappers.getMapper(PasswordResetTokenMapper.class);

    @Test
    void toEntity_and_toDomain_roundTrip() {
        var token = new PasswordResetToken(
                PasswordResetTokenId.of(UUID.randomUUID()),
                UserId.of(UUID.randomUUID()),
                "hash-abc",
                Instant.now().plusSeconds(3600),
                null,
                Instant.now()
        );

        // domain -> entity
        PasswordResetTokenEntity e = mapper.toEntity(token);
        assertEquals(token.id().value(), e.getId());
        assertEquals(token.userId().value(), e.getUserId());
        assertEquals(token.tokenHash(), e.getTokenHash());
        assertEquals(token.expiresAt(), e.getExpiresAt());
        assertEquals(token.usedAt(), e.getUsedAt());
        assertEquals(token.createdAt(), e.getCreatedAt());

        // entity -> domain
        PasswordResetToken back = mapper.toDomain(e);
        assertEquals(token.id(), back.id());
        assertEquals(token.userId(), back.userId());
        assertEquals(token.tokenHash(), back.tokenHash());
        assertEquals(token.expiresAt(), back.expiresAt());
        assertEquals(token.usedAt(), back.usedAt());
        assertEquals(token.createdAt(), back.createdAt());
    }
}
