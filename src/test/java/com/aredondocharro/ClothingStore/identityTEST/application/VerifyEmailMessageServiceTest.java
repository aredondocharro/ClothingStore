package com.aredondocharro.ClothingStore.identityTEST.application;

import com.aredondocharro.ClothingStore.identity.application.VerifyEmailService;
import com.aredondocharro.ClothingStore.identity.domain.model.*;
import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.out.LoadUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.RefreshTokenStorePort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.RefreshTokenVerifierPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SaveUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenGeneratorPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.VerificationTokenPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.VerificationTokenInvalidException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifyEmailMessageServiceTest {

    @Mock VerificationTokenPort verifier;
    @Mock LoadUserPort loadUserPort;
    @Mock SaveUserPort saveUserPort;
    @Mock TokenGeneratorPort tokens;
    @Mock RefreshTokenStorePort refreshStore;
    @Mock RefreshTokenVerifierPort refreshVerifier;
    @Mock Clock clock;

    VerifyEmailService service;

    private static final String BCRYPT =
            "$2b$10$7EqJtq98hPqEX7fNZaFWoO5f.Pg3rQAYyu3iJ/T9Y2aXx1Z9E6iGa";

    private Instant fixedNow;

    @BeforeEach
    void setUp() {
        fixedNow = Instant.parse("2025-01-01T00:00:00Z");
        Clock fixedClock = Clock.fixed(fixedNow, ZoneOffset.UTC);

        service = new VerifyEmailService(
                verifier, loadUserPort, saveUserPort, tokens, refreshStore, refreshVerifier, fixedClock
        );
    }

    @Test
    void verify_success_marksVerified_generatesTokens_andPersistsRefreshSession() {
        UserId userId = UserId.newId();
        when(verifier.validateAndExtractUserId("tok")).thenReturn(userId.value());

        User notVerified = new User(
                userId,
                IdentityEmail.of("user@example.com"),
                PasswordHash.ofHashed(BCRYPT),
                false,
                Set.of(Role.USER),
                fixedNow.minusSeconds(3600)
        );
        when(loadUserPort.findById(userId)).thenReturn(Optional.of(notVerified));
        when(saveUserPort.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        when(tokens.generateAccessToken(any(User.class))).thenReturn("access.jwt");
        when(tokens.generateRefreshToken(any(User.class))).thenReturn("refresh.jwt");

        // Decodificado del refresh emitido (usar el record del nuevo puerto)
        Instant exp = fixedNow.plusSeconds(3600);
        var decoded = new RefreshTokenVerifierPort.DecodedRefresh(userId, "jti-123", fixedNow, exp);
        when(refreshVerifier.verify("refresh.jwt")).thenReturn(decoded);

        AuthResult result = service.verify("tok");

        assertEquals("access.jwt", result.accessToken());
        assertEquals("refresh.jwt", result.refreshToken());

        // Capturamos la sesión guardada
        ArgumentCaptor<RefreshSession> cap = ArgumentCaptor.forClass(RefreshSession.class);
        verify(refreshStore).saveNew(cap.capture(), eq("refresh.jwt"));
        RefreshSession saved = cap.getValue();
        assertEquals("jti-123", saved.jti());
        assertEquals(userId, saved.userId());
        assertEquals(exp.getEpochSecond(), saved.expiresAt().getEpochSecond());
        assertNotNull(saved.createdAt());
        assertNull(saved.revokedAt());
        assertNull(saved.replacedByJti());

        // Orden principal
        InOrder io = inOrder(verifier, loadUserPort, saveUserPort, tokens, refreshVerifier, refreshStore);
        io.verify(verifier).validateAndExtractUserId("tok");
        io.verify(loadUserPort).findById(userId);
        io.verify(saveUserPort).save(argThat(u -> u.id().equals(userId) && u.emailVerified()));
        io.verify(tokens).generateAccessToken(argThat(User::emailVerified));
        io.verify(tokens).generateRefreshToken(argThat(User::emailVerified));
        io.verify(refreshVerifier).verify("refresh.jwt");
        io.verify(refreshStore).saveNew(any(RefreshSession.class), eq("refresh.jwt"));
        io.verifyNoMoreInteractions();
    }

    @Test
    void verify_alreadyVerified_generatesTokens_andPersistsRefreshSession_withoutSavingUser() {
        UserId userId = UserId.of(UUID.randomUUID());
        when(verifier.validateAndExtractUserId("tok")).thenReturn(userId.value());

        User already = new User(
                userId,
                IdentityEmail.of("user@example.com"),
                PasswordHash.ofHashed(BCRYPT),
                true,
                Set.of(Role.USER),
                fixedNow.minusSeconds(7200)
        );
        when(loadUserPort.findById(userId)).thenReturn(Optional.of(already));

        when(tokens.generateAccessToken(already)).thenReturn("access");
        when(tokens.generateRefreshToken(already)).thenReturn("refresh");

        Instant exp = fixedNow.plusSeconds(1800);
        var decoded = new RefreshTokenVerifierPort.DecodedRefresh(userId, "jti-xyz", fixedNow, exp);
        when(refreshVerifier.verify("refresh")).thenReturn(decoded);

        AuthResult result = service.verify("tok");

        assertEquals("access", result.accessToken());
        assertEquals("refresh", result.refreshToken());

        // no guarda el usuario de nuevo
        verify(saveUserPort, never()).save(any());

        // sí persiste la sesión de refresh
        ArgumentCaptor<RefreshSession> cap = ArgumentCaptor.forClass(RefreshSession.class);
        verify(refreshStore).saveNew(cap.capture(), eq("refresh"));
        RefreshSession saved = cap.getValue();
        assertEquals("jti-xyz", saved.jti());
        assertEquals(userId, saved.userId());
        assertEquals(exp, saved.expiresAt());
    }

    @Test
    void verify_userNotFound_throwsVerificationTokenInvalid_andDoesNotGenerateTokensNorPersist() {
        UUID uuid = UUID.randomUUID();
        when(verifier.validateAndExtractUserId("bad")).thenReturn(uuid);
        when(loadUserPort.findById(UserId.of(uuid))).thenReturn(Optional.empty());

        assertThrows(VerificationTokenInvalidException.class, () -> service.verify("bad"));

        verifyNoInteractions(saveUserPort, tokens, refreshVerifier, refreshStore);
    }
}
