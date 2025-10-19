package com.aredondocharro.ClothingStore.identity.domain.port.out;

import com.aredondocharro.ClothingStore.identity.domain.model.UserId;

import java.util.UUID;

public interface SessionManagerPort {
    void revokeAllSessions(UserId userId);
}