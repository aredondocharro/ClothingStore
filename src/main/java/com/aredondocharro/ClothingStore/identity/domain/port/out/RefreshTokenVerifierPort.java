package com.aredondocharro.ClothingStore.identity.domain.port.out;

import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import java.time.Instant;

public interface RefreshTokenVerifierPort {
    record DecodedRefresh(UserId userId, String jti, Instant issuedAt, Instant expiresAt) {}
    DecodedRefresh verify(String rawRefreshToken);
}
