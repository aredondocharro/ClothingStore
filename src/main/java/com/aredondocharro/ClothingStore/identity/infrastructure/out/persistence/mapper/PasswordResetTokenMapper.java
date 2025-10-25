package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.mapper;

import com.aredondocharro.ClothingStore.identity.domain.model.PasswordResetTokenId;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordResetTokenRepositoryPort.Token;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.PasswordResetTokenEntity;

public final class PasswordResetTokenMapper {
    private PasswordResetTokenMapper() {}

    public static PasswordResetTokenEntity toEntity(Token t) {
        if (t == null) return null;
        return PasswordResetTokenEntity.builder()
                .id(t.id().value())
                .userId(t.userId().value())
                .tokenHash(t.tokenHash())
                .expiresAt(t.expiresAt())
                .usedAt(t.usedAt())
                .createdAt(t.createdAt())
                .build();
    }

    public static Token toToken(PasswordResetTokenEntity e) {
        if (e == null) return null;
        return new Token(
                PasswordResetTokenId.of(e.getId()),
                UserId.of(e.getUserId()),
                e.getTokenHash(),
                e.getExpiresAt(),
                e.getUsedAt(),
                e.getCreatedAt()
        );
    }
}
