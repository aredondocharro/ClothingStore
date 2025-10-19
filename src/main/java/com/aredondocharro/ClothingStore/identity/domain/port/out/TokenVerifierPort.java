package com.aredondocharro.ClothingStore.identity.domain.port.out;

import com.aredondocharro.ClothingStore.identity.domain.model.UserId;

import java.time.Instant;


public interface TokenVerifierPort {

    record DecodedToken(
            UserId userId,
            String jti,
            Instant createdAt,
            Instant expiresAt
    ) {}

    DecodedToken verify(String token, String expectedType); // e.g. "refresh"
}
