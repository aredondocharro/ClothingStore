package com.aredondocharro.ClothingStore.identity.domain.port.out;

import java.time.Instant;
import java.util.UUID;

public interface TokenVerifierPort {

    record DecodedToken(
            UUID userId,
            String jti,
            Instant expiresAt
    ) {}

    DecodedToken verify(String token, String expectedType); // e.g. "refresh"
}
