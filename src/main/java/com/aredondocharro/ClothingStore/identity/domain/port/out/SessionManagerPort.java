package com.aredondocharro.ClothingStore.identity.domain.port.out;

import java.util.UUID;

public interface SessionManagerPort {
    void revokeAllSessions(UUID userId);
}