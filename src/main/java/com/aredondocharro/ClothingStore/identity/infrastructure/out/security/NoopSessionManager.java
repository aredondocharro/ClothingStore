package com.aredondocharro.ClothingStore.identity.infrastructure.out.security;

import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SessionManagerPort;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoopSessionManager implements SessionManagerPort {

    @Override
    public void revokeAllSessions(UserId userId) {
        // We don't log any token contents here; only a minimal identifier.
        log.info("Requested to revoke all sessions for userId={} — NO-OP (nothing revoked).", userId);
        // No-op until you manage persistent refresh tokens/sessions.
    }
}
