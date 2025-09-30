package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.domain.exception.VerificationTokenInvalidException;
import com.aredondocharro.ClothingStore.identity.domain.model.Email;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordHash;
import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.out.LoadUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SaveUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenGeneratorPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.VerificationTokenPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifyEmailServiceTest {

    @Mock VerificationTokenPort verifier;
    @Mock LoadUserPort loadUserPort;
    @Mock SaveUserPort saveUserPort;
    @Mock TokenGeneratorPort tokens;

    VerifyEmailService service;

    // Bcrypt válido (60 chars) para construir PasswordHash
    private static final String BCRYPT =
            "$2b$10$7EqJtq98hPqEX7fNZaFWoO5f.Pg3rQAYyu3iJ/T9Y2aXx1Z9E6iGa";

    @BeforeEach
    void setUp() {
        service = new VerifyEmailService(verifier, loadUserPort, saveUserPort, tokens);
    }

    @Test
    void verify_success_marksVerified_and_generatesTokens() {
        // Arrange
        var userId = UUID.randomUUID();
        when(verifier.validateAndExtractUserId("tok")).thenReturn(userId);

        var notVerified = new User(
                userId,
                Email.of("user@example.com"),
                PasswordHash.ofHashed(BCRYPT),
                false,
                Set.of(Role.USER),
                Instant.now()
        );

        when(loadUserPort.findById(userId)).thenReturn(Optional.of(notVerified));
        // save devuelve el argumento (ya verificado por el método de dominio user.verified())
        when(saveUserPort.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        when(tokens.generateAccessToken(any(User.class))).thenReturn("access.jwt");
        when(tokens.generateRefreshToken(any(User.class))).thenReturn("refresh.jwt");

        // Act
        AuthResult result = service.verify("tok");

        // Assert
        assertEquals("access.jwt", result.accessToken());
        assertEquals("refresh.jwt", result.refreshToken());

        // Verifica orden e interacciones clave
        InOrder io = inOrder(verifier, loadUserPort, saveUserPort, tokens);
        io.verify(verifier).validateAndExtractUserId("tok");
        io.verify(loadUserPort).findById(userId);
        // Debe guardar un usuario ya verificado
        io.verify(saveUserPort).save(argThat(u -> u.id().equals(userId) && u.emailVerified()));
        // Genera tokens para el usuario verificado
        io.verify(tokens).generateAccessToken(argThat(User::emailVerified));
        io.verify(tokens).generateRefreshToken(argThat(User::emailVerified));
        io.verifyNoMoreInteractions();
    }

    @Test
    void verify_alreadyVerified_doesNotSave_but_generatesTokens() {
        var userId = UUID.randomUUID();
        when(verifier.validateAndExtractUserId("tok")).thenReturn(userId);

        var already = new User(
                userId,
                Email.of("user@example.com"),
                PasswordHash.ofHashed(BCRYPT),
                true,
                Set.of(Role.USER),
                Instant.now()
        );
        when(loadUserPort.findById(userId)).thenReturn(Optional.of(already));
        when(tokens.generateAccessToken(already)).thenReturn("access");
        when(tokens.generateRefreshToken(already)).thenReturn("refresh");

        AuthResult result = service.verify("tok");

        assertEquals("access", result.accessToken());
        assertEquals("refresh", result.refreshToken());

        verify(saveUserPort, never()).save(any()); // no guarda de nuevo
        verify(tokens).generateAccessToken(already);
        verify(tokens).generateRefreshToken(already);
    }

    @Test
    void verify_userNotFound_throwsVerificationTokenInvalid_and_doesNotCallSaveOrTokens() {
        var userId = UUID.randomUUID();
        when(verifier.validateAndExtractUserId("bad")).thenReturn(userId);
        when(loadUserPort.findById(userId)).thenReturn(Optional.empty());

        assertThrows(VerificationTokenInvalidException.class, () -> service.verify("bad"));

        verifyNoInteractions(saveUserPort, tokens);
    }
}
