package com.aredondocharro.ClothingStore.identityTEST.infrastructure.out.persistence.mapper;

import com.aredondocharro.ClothingStore.identity.domain.model.RefreshSession;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.RefreshSessionEntity;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.mapper.RefreshSessionEntityMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RefreshSessionEntityMapperTest {

    @Test
    void toDomain_and_toEntity_mapAllFields() {
        Instant now = Instant.now();
        RefreshSession session = new RefreshSession(
                "jti-123",
                UserId.of(UUID.randomUUID()),
                now.plusSeconds(3600),
                now,
                null,            // revokedAt
                "replaced-456",  // replacedByJti
                "127.0.0.1",
                "JUnit UA"
        );

        RefreshSessionEntity entity = RefreshSessionEntityMapper.toEntity(session);
        assertEquals(session.jti(), entity.getJti());
        assertEquals(session.userId().value(), entity.getUserId());
        assertEquals(session.expiresAt(), entity.getExpiresAt());
        assertEquals(session.createdAt(), entity.getCreatedAt());
        assertEquals(session.revokedAt(), entity.getRevokedAt());
        assertEquals(session.replacedByJti(), entity.getReplacedByJti());
        assertEquals(session.ip(), entity.getIp());
        assertEquals(session.userAgent(), entity.getUserAgent());

        RefreshSession back = RefreshSessionEntityMapper.toDomain(entity);
        assertEquals(session.jti(), back.jti());
        assertEquals(session.userId(), back.userId());
        assertEquals(session.expiresAt(), back.expiresAt());
        assertEquals(session.createdAt(), back.createdAt());
        assertEquals(session.revokedAt(), back.revokedAt());
        assertEquals(session.replacedByJti(), back.replacedByJti());
        assertEquals(session.ip(), back.ip());
        assertEquals(session.userAgent(), back.userAgent());
    }
}
