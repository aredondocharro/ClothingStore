package com.aredondocharro.ClothingStore.identityTEST.application;

import com.aredondocharro.ClothingStore.identity.application.PasswordRecoveryService;
import com.aredondocharro.ClothingStore.identity.domain.port.out.MailerPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordHasherPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordPolicyPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordResetTokenRepositoryPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SessionManagerPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryServiceTest {

    @Mock
    private UserRepositoryPort users;

    @Mock
    private PasswordResetTokenRepositoryPort tokens;

    @Mock
    private PasswordPolicyPort passwordPolicy;

    @Mock
    private SessionManagerPort sessions;

    @Mock
    private PasswordHasherPort passwordHasher;

    @Mock
    private MailerPort mailer;

    private PasswordRecoveryService service;

    private static final String BASE_URL_PROP = "app.reset.baseUrl";
    private static final String TEST_BASE_URL = "https://app.example/reset-password";

    @BeforeEach
    void setUp() {
        // Forzamos baseUrl conocida para poder verificar el link y extraer el raw token
        System.setProperty(BASE_URL_PROP, TEST_BASE_URL);
        service = new PasswordRecoveryService(
                users, tokens, passwordPolicy, mailer, sessions, passwordHasher, TEST_BASE_URL
        );
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(BASE_URL_PROP);
    }

    @Nested
    class RequestReset {

        @Test
        void whenUserExists_savesToken_deletesOld_sendsEmail() {
            UUID userId = UUID.randomUUID();
            String email = "user@example.com";
            UserRepositoryPort.UserView userView =
                    new UserRepositoryPort.UserView(userId, email, "hash", true);

            when(users.findByEmail(email)).thenReturn(Optional.of(userView));

            ArgumentCaptor<PasswordResetTokenRepositoryPort.Token> tokenCap =
                    ArgumentCaptor.forClass(PasswordResetTokenRepositoryPort.Token.class);
            ArgumentCaptor<String> emailCap = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> linkCap = ArgumentCaptor.forClass(String.class);

            // Act
            service.requestReset(email);

            // Assert: flujo principal
            InOrder inOrder = inOrder(tokens, mailer);
            inOrder.verify(tokens).deleteAllForUser(eq(userId));
            inOrder.verify(tokens).save(tokenCap.capture());
            inOrder.verify(mailer).sendPasswordResetLink(emailCap.capture(), linkCap.capture());

            assertEquals(email, emailCap.getValue());

            // Validar que el link contiene el token como query param
            String link = linkCap.getValue();
            assertTrue(link.startsWith(TEST_BASE_URL + "?token="), "Reset link must start with baseUrl + '?token='");

            String rawToken = link.substring((TEST_BASE_URL + "?token=").length());
            assertFalse(rawToken.isBlank(), "Raw token must be present in the link");

            // Verificar token persistido
            PasswordResetTokenRepositoryPort.Token saved = tokenCap.getValue();
            assertEquals(userId, saved.userId());
            assertNull(saved.usedAt());
            assertNotNull(saved.createdAt());
            assertNotNull(saved.expiresAt());

            // TTL ~30 minutos (tolerancia ±3 min)
            Instant now = Instant.now();
            long minutes = Duration.between(now, saved.expiresAt()).toMinutes();
            assertTrue(minutes >= 27 && minutes <= 33, "Token TTL should be ~30 minutes");

            // Hash correcto del rawToken
            String expectedHash = sha256Base64(rawToken);
            assertEquals(expectedHash, saved.tokenHash(), "Persisted token hash must match SHA-256(rawToken) in Base64");
        }

        @Test
        void whenUserNotFound_doNothingVisible() {
            when(users.findByEmail("missing@example.com")).thenReturn(Optional.empty());

            service.requestReset("missing@example.com");

            verify(tokens, never()).save(any());
            verify(tokens, never()).deleteAllForUser(any());
            verify(mailer, never()).sendPasswordResetLink(anyString(), anyString());
        }
    }

    @Nested
    class ResetPassword {

        @Test
        void withValidToken_updatesPassword_marksUsed_revokesSessions() {
            // Arrange
            UUID userId = UUID.randomUUID();
            String newPassword = "NewPass123!";
            String anyRawToken = "whatever-raw-token"; // el service hasheará internamente
            String hashedNew = "HASHED_NEW";

            Instant now = Instant.now();
            PasswordResetTokenRepositoryPort.Token token =
                    new PasswordResetTokenRepositoryPort.Token(
                            UUID.randomUUID(), userId, "HASHED_TOKEN", now.plus(30, ChronoUnit.MINUTES), null, now
                    );

            when(tokens.findValidByHash(anyString(), any(Instant.class)))
                    .thenReturn(Optional.of(token));
            when(passwordHasher.hash(newPassword)).thenReturn(hashedNew);

            // Act
            service.reset(anyRawToken, newPassword);

            // Assert: se valida política, se actualiza password, se marca usado y se revocan sesiones
            verify(passwordPolicy).validate(eq(newPassword));
            InOrder inOrder = inOrder(users, tokens, sessions);
            inOrder.verify(users).updatePasswordHash(eq(userId), eq(hashedNew));
            inOrder.verify(tokens).markUsed(eq(token.id()), any(Instant.class));
            inOrder.verify(sessions).revokeAllSessions(eq(userId));
        }

        @Test
        void withInvalidToken_throwsIllegalArgumentException() {
            when(tokens.findValidByHash(anyString(), any(Instant.class))).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> service.reset("bad-token", "NewPass123!"));

            verify(users, never()).updatePasswordHash(any(), any());
            verify(tokens, never()).markUsed(any(), any());
            verify(sessions, never()).revokeAllSessions(any());
        }
    }

    @Nested
    class ChangePassword {

        @Test
        void withCorrectCurrent_updatesAndRevokesSessions() {
            UUID userId = UUID.randomUUID();
            String current = "Old123!";
            String currentHash = "OLD_HASH";
            String next = "NewPass123!";
            String nextHash = "NEW_HASH";

            UserRepositoryPort.UserView userView =
                    new UserRepositoryPort.UserView(userId, "u@example.com", currentHash, true);

            when(users.findById(userId)).thenReturn(Optional.of(userView));
            when(passwordHasher.matches(current, currentHash)).thenReturn(true);
            when(passwordHasher.hash(next)).thenReturn(nextHash);

            service.change(userId, current, next);

            verify(passwordPolicy).validate(eq(next));
            InOrder inOrder = inOrder(users, sessions);
            inOrder.verify(users).updatePasswordHash(eq(userId), eq(nextHash));
            inOrder.verify(sessions).revokeAllSessions(eq(userId));
        }

        @Test
        void withWrongCurrent_throws_andDoesNotUpdate() {
            UUID userId = UUID.randomUUID();
            String current = "Wrong!";
            String storedHash = "STORED";
            String next = "NewPass123!";

            UserRepositoryPort.UserView userView =
                    new UserRepositoryPort.UserView(userId, "u@example.com", storedHash, true);

            when(users.findById(userId)).thenReturn(Optional.of(userView));
            when(passwordHasher.matches(current, storedHash)).thenReturn(false);

            assertThrows(IllegalArgumentException.class, () -> service.change(userId, current, next));

            verify(users, never()).updatePasswordHash(any(), any());
            verify(sessions, never()).revokeAllSessions(any());
        }
    }

    // =================== helpers ===================

    private static String sha256Base64(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
