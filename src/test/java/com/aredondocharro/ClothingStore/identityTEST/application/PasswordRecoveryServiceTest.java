package com.aredondocharro.ClothingStore.identityTEST.application;

import com.aredondocharro.ClothingStore.identity.application.ChangePasswordService;
import com.aredondocharro.ClothingStore.identity.application.RequestPasswordResetService;
import com.aredondocharro.ClothingStore.identity.application.ResetPasswordService;
import com.aredondocharro.ClothingStore.identity.domain.exception.NewPasswordSameAsOldException;
import com.aredondocharro.ClothingStore.identity.domain.exception.PasswordResetTokenInvalidException;
import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordHash;
import com.aredondocharro.ClothingStore.identity.domain.port.out.MailerPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordHasherPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordPolicyPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordResetTokenRepositoryPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SessionManagerPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.UserRepositoryPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.view.CredentialsView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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

    @Mock private UserRepositoryPort users;
    @Mock private PasswordResetTokenRepositoryPort tokens;
    @Mock private PasswordPolicyPort passwordPolicy;
    @Mock private SessionManagerPort sessions;
    @Mock private PasswordHasherPort passwordHasher;
    @Mock private MailerPort mailer;

    // Servicios refactorizados
    private RequestPasswordResetService requestPasswordResetService;
    private ResetPasswordService resetPasswordService;
    private ChangePasswordService changePasswordService;

    private static final String BASE_URL_PROP = "app.reset.baseUrl";
    private static final String TEST_BASE_URL = "https://app.example/reset-password";
    private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder(10);

    @BeforeEach
    void setUp() {
        System.setProperty(BASE_URL_PROP, TEST_BASE_URL);

        requestPasswordResetService = new RequestPasswordResetService(
                users, tokens, mailer, TEST_BASE_URL
        );
        resetPasswordService = new ResetPasswordService(
                tokens, passwordPolicy, users, passwordHasher, sessions
        );
        changePasswordService = new ChangePasswordService(
                users, passwordHasher, passwordPolicy, sessions
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
            IdentityEmail email = IdentityEmail.of("user@example.com");

            // Hash bcrypt REAL y válido para el usuario existente
            PasswordHash hash = hashOf("Old123!");
            CredentialsView cw = new CredentialsView(userId, email, hash, true);

            when(users.findByEmail(email.getValue())).thenReturn(Optional.of(cw));

            ArgumentCaptor<PasswordResetTokenRepositoryPort.Token> tokenCap =
                    ArgumentCaptor.forClass(PasswordResetTokenRepositoryPort.Token.class);
            ArgumentCaptor<String> emailCap = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> linkCap = ArgumentCaptor.forClass(String.class);

            // Act
            requestPasswordResetService.requestReset(email.getValue());

            // Assert: flujo principal
            InOrder inOrder = inOrder(tokens, mailer);
            inOrder.verify(tokens).deleteAllForUser(eq(userId));
            inOrder.verify(tokens).save(tokenCap.capture());
            inOrder.verify(mailer).sendPasswordResetLink(emailCap.capture(), linkCap.capture());

            assertEquals(email.getValue(), emailCap.getValue());

            // Validar que el link contiene el token como query param
            String link = linkCap.getValue();
            assertTrue(link.startsWith(TEST_BASE_URL + "?token="),
                    "Reset link must start with baseUrl + '?token='");

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
            assertEquals(expectedHash, saved.tokenHash(),
                    "Persisted token hash must match SHA-256(rawToken) in Base64");
        }

        @Test
        void whenUserNotFound_doNothingVisible() {
            when(users.findByEmail("missing@example.com")).thenReturn(Optional.empty());

            requestPasswordResetService.requestReset("missing@example.com");

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
            String anyRawToken = "whatever-raw-token"; // el servicio hasheará internamente

            // Hash bcrypt REAL para la nueva contraseña
            String hashedNew = BCRYPT.encode(newPassword);

            Instant now = Instant.now();
            PasswordResetTokenRepositoryPort.Token token =
                    new PasswordResetTokenRepositoryPort.Token(
                            UUID.randomUUID(), userId, "HASHED_TOKEN",
                            now.plus(30, ChronoUnit.MINUTES), null, now
                    );

            when(tokens.findValidByHash(anyString(), any(Instant.class)))
                    .thenReturn(Optional.of(token));
            when(passwordHasher.hash(newPassword)).thenReturn(hashedNew);

            // Act
            resetPasswordService.reset(anyRawToken, newPassword);

            // Assert
            verify(passwordPolicy).validate(eq(newPassword));
            InOrder inOrder = inOrder(users, tokens, sessions);
            inOrder.verify(users).updatePasswordHash(eq(userId), eq(hashedNew));
            inOrder.verify(tokens).markUsed(eq(token.id()), any(Instant.class));
            inOrder.verify(sessions).revokeAllSessions(eq(userId));
        }

        @Test
        void withInvalidToken_throwsIllegalArgumentException() {
            when(tokens.findValidByHash(anyString(), any(Instant.class))).thenReturn(Optional.empty());

            assertThrows(PasswordResetTokenInvalidException.class,
                    () -> resetPasswordService.reset("bad-token", "NewPass123!"));

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
            String currentPlaintext = "Old123!";   // contraseña actual (raw)
            PasswordHash currentHash = hashOf(currentPlaintext);
            String nextPlaintext = "NewPass123!";  // nueva contraseña (raw)
            String nextHashValue = BCRYPT.encode(nextPlaintext);

            CredentialsView cw = new CredentialsView(
                    userId,
                    IdentityEmail.of("u@example.com"),
                    currentHash,
                    true
            );

            when(users.findById(userId)).thenReturn(Optional.of(cw));
            when(passwordHasher.matches(currentPlaintext, currentHash.getValue())).thenReturn(true);
            when(passwordHasher.hash(nextPlaintext)).thenReturn(nextHashValue);

            changePasswordService.change(userId, currentPlaintext, nextPlaintext);

            verify(passwordPolicy).validate(eq(nextPlaintext));
            InOrder inOrder = inOrder(users, sessions);
            inOrder.verify(users).updatePasswordHash(eq(userId), eq(nextHashValue));
            inOrder.verify(sessions).revokeAllSessions(eq(userId));
        }

        @Test
        void withWrongCurrent_throws_andDoesNotUpdate() {
            UUID userId = UUID.randomUUID();
            String current = "Wrong!";
            PasswordHash storedHash = hashOf("Old123!");
            String next = "NewPass123!";

            CredentialsView userView =
                    new CredentialsView(userId, IdentityEmail.of("u@example.com"), storedHash, true);

            when(users.findById(userId)).thenReturn(Optional.of(userView));
            when(passwordHasher.matches(current, storedHash.getValue())).thenReturn(false);

            assertThrows(NewPasswordSameAsOldException.class,
                    () -> changePasswordService.change(userId, current, next));

            verify(users, never()).updatePasswordHash(any(), any());
            verify(sessions, never()).revokeAllSessions(any());
        }
    }

    // =================== helpers ===================
    private static PasswordHash hashOf(String raw) {
        return PasswordHash.ofHashed(BCRYPT.encode(raw));
    }

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
