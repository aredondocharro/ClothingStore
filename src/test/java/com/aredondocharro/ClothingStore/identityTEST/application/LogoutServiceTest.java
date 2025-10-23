package com.aredondocharro.ClothingStore.identityTEST.application;

import com.aredondocharro.ClothingStore.identity.application.LogoutService;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.out.RefreshTokenStorePort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.RefreshTokenVerifierPort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class LogoutServiceTest {

    @Test
    void logout_revokes_session() {
        // mocks
        RefreshTokenVerifierPort refreshVerifier = mock(RefreshTokenVerifierPort.class);
        RefreshTokenStorePort store = mock(RefreshTokenStorePort.class);

        // datos
        UserId userId = UserId.of(UUID.randomUUID());
        Instant issuedAt = Instant.parse("2025-01-01T00:00:00Z");
        Instant expiresAt = issuedAt.plusSeconds(3600);

        when(refreshVerifier.verify(eq("R1")))
                .thenReturn(new RefreshTokenVerifierPort.DecodedRefresh(
                        userId, "JTI1", issuedAt, expiresAt
                ));

        // SUT
        LogoutService service = new LogoutService(refreshVerifier, store);

        // act
        service.logout("R1", "1.2.3.4");

        // assert
        verify(refreshVerifier).verify(eq("R1"));

        ArgumentCaptor<Instant> nowCap = ArgumentCaptor.forClass(Instant.class);
        verify(store).revoke(eq("JTI1"), eq("logout"), nowCap.capture());

        assertNotNull(nowCap.getValue()); // el servicio usa Instant.now() internamente
        verifyNoMoreInteractions(refreshVerifier, store);
    }
}
