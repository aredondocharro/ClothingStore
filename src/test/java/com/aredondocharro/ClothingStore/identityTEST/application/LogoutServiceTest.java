package com.aredondocharro.ClothingStore.identityTEST.application;


import com.aredondocharro.ClothingStore.identity.application.LogoutService;
import com.aredondocharro.ClothingStore.identity.domain.port.out.RefreshTokenStorePort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenVerifierPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

class LogoutServiceTest {

    @Test
    void logout_revokes_session() {
        var verifier = mock(TokenVerifierPort.class);
        var store = mock(RefreshTokenStorePort.class);

        var userId = UUID.randomUUID();
        when(verifier.verify(eq("R1"), eq("refresh")))
                .thenReturn(new TokenVerifierPort.DecodedToken(userId, "JTI1", Instant.now().plusSeconds(3600)));

        var service = new LogoutService(verifier, store);
        service.logout("R1", "1.2.3.4");

        verify(store).revoke(eq("JTI1"), eq("logout"), any());
    }
}
