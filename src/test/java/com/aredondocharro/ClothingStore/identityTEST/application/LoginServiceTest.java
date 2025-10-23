package com.aredondocharro.ClothingStore.identityTEST.application;

import com.aredondocharro.ClothingStore.identity.application.LoginService;
import com.aredondocharro.ClothingStore.identity.domain.exception.EmailNotVerifiedException;
import com.aredondocharro.ClothingStore.identity.domain.exception.InvalidCredentialsException;
import com.aredondocharro.ClothingStore.identity.domain.exception.PasswordRequiredException;
import com.aredondocharro.ClothingStore.identity.domain.model.*;
import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.out.LoadUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordHasherPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.RefreshTokenStorePort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenGeneratorPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenVerifierPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;              // ⬅️ nuevo
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    LoadUserPort loadUserPort;
    @Mock
    PasswordHasherPort hasher;
    @Mock
    TokenGeneratorPort tokens;
    @Mock
    RefreshTokenStorePort refreshStore;
    @Mock
    TokenVerifierPort tokenVerifier;
    @Mock
    java.time.Clock clock;
    LoginService service;

    @BeforeEach
    void setUp() {
        service = new LoginService(loadUserPort, hasher, tokens, refreshStore, tokenVerifier, clock);
    }

    @Test
    void login_nullOrBlankPassword_throwsPasswordRequired() {
        IdentityEmail email = IdentityEmail.of("user@example.com");

        assertAll(
                () -> assertThrows(PasswordRequiredException.class, () -> service.login(email, null)),
                () -> assertThrows(PasswordRequiredException.class, () -> service.login(email, "")),
                () -> assertThrows(PasswordRequiredException.class, () -> service.login(email, "   "))
        );

        verifyNoInteractions(loadUserPort, hasher, tokens, tokenVerifier, refreshStore);
    }

    @Test
    void login_userNotFound_throwsInvalidCredentials() {
        IdentityEmail email = IdentityEmail.of("user@example.com");
        when(loadUserPort.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> service.login(email, "Secret123!"));

        verify(loadUserPort).findByEmail(email);
        verifyNoMoreInteractions(loadUserPort);
        verifyNoInteractions(hasher, tokens, tokenVerifier, refreshStore);
    }

    @Test
    void login_badPassword_throwsInvalidCredentials() {
        IdentityEmail email = IdentityEmail.of("user@example.com");

        User user = mock(User.class);
        PasswordHash ph = mock(PasswordHash.class);
        when(user.passwordHash()).thenReturn(ph);
        when(ph.getValue()).thenReturn("$2b$10$whateverhashstring................................");

        when(loadUserPort.findByEmail(email)).thenReturn(Optional.of(user));
        when(hasher.matches("WrongPass", ph.getValue())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> service.login(email, "WrongPass"));

        InOrder inOrder = inOrder(loadUserPort, hasher);
        inOrder.verify(loadUserPort).findByEmail(email);
        inOrder.verify(hasher).matches("WrongPass", ph.getValue());

        verifyNoInteractions(tokens, tokenVerifier, refreshStore);
    }

    @Test
    void login_emailNotVerified_throwsEmailNotVerified() {
        IdentityEmail email = IdentityEmail.of("user@example.com");

        User user = mock(User.class);
        PasswordHash ph = mock(PasswordHash.class);
        when(user.passwordHash()).thenReturn(ph);
        when(ph.getValue()).thenReturn("$2b$10$whateverhashstring................................");
        when(user.emailVerified()).thenReturn(false); // no verificado

        when(loadUserPort.findByEmail(email)).thenReturn(Optional.of(user));
        when(hasher.matches("Secret123!", ph.getValue())).thenReturn(true);

        assertThrows(EmailNotVerifiedException.class, () -> service.login(email, "Secret123!"));

        InOrder inOrder = inOrder(loadUserPort, hasher);
        inOrder.verify(loadUserPort).findByEmail(email);
        inOrder.verify(hasher).matches("Secret123!", ph.getValue());

        verifyNoInteractions(tokens, tokenVerifier, refreshStore);
    }

    @Test
    void login_success_generatesTokens_verifiesRefresh_andPersistsSession() {
        IdentityEmail email = IdentityEmail.of("user@example.com");

        User user = mock(User.class);
        PasswordHash ph = mock(PasswordHash.class);
        UUID rawUserId = UUID.randomUUID();

        when(user.passwordHash()).thenReturn(ph);
        when(ph.getValue()).thenReturn("$2b$10$whateverhashstring................................");
        when(user.emailVerified()).thenReturn(true);
        when(user.id()).thenReturn(UserId.of(rawUserId));

        when(loadUserPort.findByEmail(email)).thenReturn(Optional.of(user));
        when(hasher.matches("Secret123!", ph.getValue())).thenReturn(true);

        when(tokens.generateAccessToken(user)).thenReturn("access.jwt.token");
        when(tokens.generateRefreshToken(user)).thenReturn("refresh.jwt.token");

        // ⬇️ Fijamos el Clock que usa el servicio
        Instant fixedNow = Instant.parse("2025-01-01T00:00:00Z");
        when(clock.instant()).thenReturn(fixedNow);

        // exp > createdAt para respetar el invariante de RefreshSession
        Instant exp = fixedNow.plusSeconds(3600);
        var decoded = new TokenVerifierPort.DecodedToken(
                UserId.of(rawUserId),
                "jti-123",
                null,          // iat puede faltar; el servicio usa now(clock)
                exp,
                List.of()      // ⬅️ nuevo: authorities (vacío por ahora)
        );
        when(tokenVerifier.verify("refresh.jwt.token", "refresh")).thenReturn(decoded);

        AuthResult result = service.login(email, "Secret123!");

        assertNotNull(result);
        assertEquals("access.jwt.token", result.accessToken());
        assertEquals("refresh.jwt.token", result.refreshToken());

        ArgumentCaptor<RefreshSession> cap = ArgumentCaptor.forClass(RefreshSession.class);
        InOrder inOrder = inOrder(loadUserPort, hasher, tokens, tokenVerifier, refreshStore);
        inOrder.verify(loadUserPort).findByEmail(email);
        inOrder.verify(hasher).matches("Secret123!", ph.getValue());
        inOrder.verify(tokens).generateAccessToken(user);
        inOrder.verify(tokens).generateRefreshToken(user);
        inOrder.verify(tokenVerifier).verify("refresh.jwt.token", "refresh");
        inOrder.verify(refreshStore).saveNew(cap.capture(), eq("refresh.jwt.token"));
        inOrder.verifyNoMoreInteractions();

        RefreshSession saved = cap.getValue();
        assertEquals("jti-123", saved.jti());
        assertEquals(UserId.of(rawUserId), saved.userId());
        assertEquals(exp, saved.expiresAt());
        assertEquals(fixedNow, saved.createdAt());   // ✅ ahora no es null
        assertNull(saved.revokedAt());
        assertNull(saved.replacedByJti());
    }
}
