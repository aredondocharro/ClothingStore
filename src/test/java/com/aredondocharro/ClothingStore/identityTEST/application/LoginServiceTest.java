package com.aredondocharro.ClothingStore.identityTEST.application;

import com.aredondocharro.ClothingStore.identity.application.LoginService;
import com.aredondocharro.ClothingStore.identity.domain.exception.EmailNotVerifiedException;
import com.aredondocharro.ClothingStore.identity.domain.exception.InvalidCredentialsException;
import com.aredondocharro.ClothingStore.identity.domain.exception.PasswordRequiredException;
import com.aredondocharro.ClothingStore.identity.domain.model.Email;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordHash;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.out.LoadUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordHasherPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenGeneratorPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock LoadUserPort loadUserPort;
    @Mock PasswordHasherPort hasher;
    @Mock TokenGeneratorPort tokens;

    LoginService service;

    @BeforeEach
    void setUp() {
        service = new LoginService(loadUserPort, hasher, tokens);
    }

    @Test
    void login_nullOrBlankPassword_throwsPasswordRequired() {
        var email = Email.of("user@example.com");

        assertAll(
                () -> assertThrows(PasswordRequiredException.class, () -> service.login(email, null)),
                () -> assertThrows(PasswordRequiredException.class, () -> service.login(email, "")),
                () -> assertThrows(PasswordRequiredException.class, () -> service.login(email, "   "))
        );

        verifyNoInteractions(loadUserPort, hasher, tokens);
    }

    @Test
    void login_userNotFound_throwsInvalidCredentials() {
        var email = Email.of("user@example.com");
        when(loadUserPort.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> service.login(email, "Secret123!"));

        verify(loadUserPort).findByEmail(email);
        verifyNoMoreInteractions(loadUserPort);
        verifyNoInteractions(hasher, tokens);
    }

    @Test
    void login_badPassword_throwsInvalidCredentials() {
        Email email = Email.of("user@example.com");


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

        verifyNoInteractions(tokens);
    }

    @Test
    void login_emailNotVerified_throwsEmailNotVerified() {
        Email email = Email.of("user@example.com");

        User user = mock(User.class);
        PasswordHash ph = mock(PasswordHash.class);
        when(user.passwordHash()).thenReturn(ph);
        when(ph.getValue()).thenReturn("$2b$10$whateverhashstring................................");
        when(user.emailVerified()).thenReturn(false); // ← no verificado

        when(loadUserPort.findByEmail(email)).thenReturn(Optional.of(user));
        when(hasher.matches("Secret123!", ph.getValue())).thenReturn(true);

        assertThrows(EmailNotVerifiedException.class, () -> service.login(email, "Secret123!"));

        InOrder inOrder = inOrder(loadUserPort, hasher);
        inOrder.verify(loadUserPort).findByEmail(email);
        inOrder.verify(hasher).matches("Secret123!", ph.getValue());

        verifyNoInteractions(tokens);
    }

    @Test
    void login_success_returnsAuthResultAndGeneratesTokens() {
        Email email = Email.of("user@example.com");

        User user = mock(User.class);
        PasswordHash ph = mock(PasswordHash.class);
        when(user.passwordHash()).thenReturn(ph);
        when(ph.getValue()).thenReturn("$2b$10$whateverhashstring................................");
        when(user.emailVerified()).thenReturn(true);

        when(loadUserPort.findByEmail(email)).thenReturn(Optional.of(user));
        when(hasher.matches("Secret123!", ph.getValue())).thenReturn(true);

        when(tokens.generateAccessToken(user)).thenReturn("access.jwt.token");
        when(tokens.generateRefreshToken(user)).thenReturn("refresh.jwt.token");

        AuthResult result = service.login(email, "Secret123!");

        assertNotNull(result);
        assertEquals("access.jwt.token", result.accessToken());
        assertEquals("refresh.jwt.token", result.refreshToken());

        InOrder inOrder = inOrder(loadUserPort, hasher, tokens);
        inOrder.verify(loadUserPort).findByEmail(email);
        inOrder.verify(hasher).matches("Secret123!", ph.getValue());
        inOrder.verify(tokens).generateAccessToken(user);
        inOrder.verify(tokens).generateRefreshToken(user);
        verifyNoMoreInteractions(loadUserPort, hasher, tokens);
    }
}
