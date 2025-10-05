package com.aredondocharro.ClothingStore.identity.infrastructure.security;

import com.aredondocharro.ClothingStore.identity.domain.port.out.SessionManagerPort;

import java.util.UUID;

public class NoopSessionManager implements SessionManagerPort {
    @Override
    public void revokeAllSessions(UUID userId) {
        // No-op hasta que gestiones refresh tokens/sesiones persistentes.
    }
}