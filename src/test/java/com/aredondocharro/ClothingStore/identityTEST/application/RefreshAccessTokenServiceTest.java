package com.aredondocharro.ClothingStore.identityTEST.application;

import com.aredondocharro.ClothingStore.identity.application.RefreshAccessTokenService;
import com.aredondocharro.ClothingStore.identity.domain.model.*;
import com.aredondocharro.ClothingStore.identity.domain.port.out.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RefreshAccessTokenServiceTest {

    @Test
    void rotatesRefreshAndSavesSession_markingOldAsReplaced() {
        // Mocks
        RefreshTokenVerifierPort verifier = mock(RefreshTokenVerifierPort.class);
        RefreshTokenStorePort store = mock(RefreshTokenStorePort.class);
        LoadUserPort loadUsers = mock(LoadUserPort.class);
        TokenGeneratorPort tokens = mock(TokenGeneratorPort.class);

        RefreshAccessTokenService svc = new RefreshAccessTokenService(verifier,store, loadUsers, tokens);

        // Data
        UserId uid = UserId.newId();
        IdentityEmail email = IdentityEmail.of("u@example.com");
        PasswordHash hash = PasswordHash.ofHashed("$2a$10$" + "a".repeat(53));
        User user = User.create(uid, email, hash, Set.of(Role.USER), Instant.now()).verified();

        String oldJti = "old-jti";
        String ip = "10.0.0.1";
        String ua = "JUnit";

        var decodedOld = new RefreshTokenVerifierPort.DecodedRefresh(uid, oldJti, Instant.now().minusSeconds(60), Instant.now().plusSeconds(600));
        when(verifier.verify("old-refresh")).thenReturn(decodedOld);

        RefreshSession existing = RefreshSession.rehydrate(oldJti, uid.value(), decodedOld.expiresAt(), decodedOld.issuedAt(), null, null, ip, ua);
        when(store.findByJti(oldJti)).thenReturn(Optional.of(existing));

        when(loadUsers.findById(uid)).thenReturn(Optional.of(user));

        when(tokens.generateAccessToken(user)).thenReturn("new-access");
        when(tokens.generateRefreshToken(user)).thenReturn("new-refresh");

        var decodedNew = new RefreshTokenVerifierPort.DecodedRefresh(uid, "new-jti", Instant.now(), Instant.now().plusSeconds(1200));
        when(verifier.verify("new-refresh")).thenReturn(decodedNew);

        // Act
        var result = svc.refresh("old-refresh", ip, ua);

        // Assert
        assertEquals("new-access", result.accessToken());
        assertEquals("new-refresh", result.refreshToken());
        verify(store).saveNew(any(RefreshSession.class), eq("new-refresh"));
        verify(store).markReplaced(eq(oldJti), eq("new-jti"), any(Instant.class));
    }
}
