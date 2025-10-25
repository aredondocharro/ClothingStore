package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.mapper;

import com.aredondocharro.ClothingStore.identity.domain.model.RefreshSession;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.RefreshSessionEntity;

public final class RefreshSessionEntityMapper {
    private RefreshSessionEntityMapper() {}

    public static RefreshSession toDomain(RefreshSessionEntity e) {
        if (e == null) return null;
        return new RefreshSession(
                e.getJti(),
                UserId.of(e.getUserId()),
                e.getExpiresAt(),
                e.getCreatedAt(),
                e.getRevokedAt(),
                e.getReplacedByJti(),
                e.getIp(),
                e.getUserAgent()
        );
    }

    public static RefreshSessionEntity toEntity(RefreshSession s) {
        if (s == null) return null;
        return RefreshSessionEntity.builder()
                .jti(s.jti())
                .userId(s.userId().value())
                .expiresAt(s.expiresAt())
                .createdAt(s.createdAt())
                .revokedAt(s.revokedAt())
                .replacedByJti(s.replacedByJti())
                .ip(s.ip())
                .userAgent(s.userAgent())
                .build();
    }
}
