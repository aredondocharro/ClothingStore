package com.aredondocharro.ClothingStore.identity.infrastructure.out.security;

import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SessionManagerPort;

public class NoopSessionManager implements SessionManagerPort {
    @Override
    public void revokeAllSessions(UserId userId) {
        // No-op hasta que gestiones refresh tokens/sesiones persistentes.
    }
}