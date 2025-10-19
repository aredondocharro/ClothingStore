package com.aredondocharro.ClothingStore.identity.domain.port.out;

import com.aredondocharro.ClothingStore.identity.domain.model.RefreshSession;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;

import java.time.Instant;
import java.util.Optional;


public interface RefreshTokenStorePort {

    Optional<RefreshSession> findByJti(String jti);

    // Guarda la nueva sesión; el adapter se encarga de hashear el raw token
    RefreshSession saveNew(RefreshSession session, String rawRefreshToken);

    void markReplaced(String oldJti, String newJti, Instant when);

    void revoke(String jti, String reason, Instant when);

    void revokeAllForUser(UserId userId, String reason, Instant when);
}
