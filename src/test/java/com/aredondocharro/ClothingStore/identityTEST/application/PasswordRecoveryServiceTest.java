package com.aredondocharro.ClothingStore.identityTEST.application;

import com.aredondocharro.ClothingStore.identity.application.ChangePasswordService;
import com.aredondocharro.ClothingStore.identity.application.RequestPasswordResetService;
import com.aredondocharro.ClothingStore.identity.application.ResetPasswordService;
import com.aredondocharro.ClothingStore.identity.contracts.event.PasswordResetEmailRequested;
import com.aredondocharro.ClothingStore.identity.domain.exception.NewPasswordSameAsOldException;
import com.aredondocharro.ClothingStore.identity.domain.exception.InvalidPasswordException;
import com.aredondocharro.ClothingStore.identity.domain.model.*;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordHasherPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordPolicyPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordResetTokenRepositoryPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SessionManagerPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.UserRepositoryPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.PasswordResetTokenInvalidException;
import com.aredondocharro.ClothingStore.identity.domain.port.out.view.CredentialsView;
import com.aredondocharro.ClothingStore.shared.domain.event.EventBusPort;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

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
    @Mock private EventBusPort eventBus;

    private RequestPasswordResetService requestPasswordResetService;
    private ResetPasswordService resetPasswordService;
    private ChangePasswordService changePasswordService;

    private static final String TEST_BASE_URL = "https://app.example/reset-password";
    private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder(10);

    private static final Instant FIXED_NOW = Instant.parse("2025-10-14T12:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
    private static final Duration TTL = Duration.ofMinutes(30);

    @BeforeEach
    void setUp() {
        requestPasswordResetService = new RequestPasswordResetService(
                users, tokens, eventBus, TEST_BASE_URL, FIXED_CLOCK, TTL
        );
        resetPasswordService = new ResetPasswordService(
                tokens, passwordPolicy, users, passwordHasher, sessions, FIXED_CLOCK
        );
        changePasswordService = new ChangePasswordService(
                users, passwordHasher, passwordPolicy, sessions
        );
    }

    @Nested
    class RequestReset {

        @Test
        void whenUserExists_persistsToken_deletesOld_andPublishesEvent() {
            UserId userId = UserId.newId();
            IdentityEmail email = IdentityEmail.of("user@example.com");

            PasswordHash hash = hashOf("Old123!");
            CredentialsView cw = new CredentialsView(userId, email, hash, true);

            when(users.findByEmail(email)).thenReturn(Optional.of(cw));

            ArgumentCaptor<PasswordResetToken> tokenCap =
                    ArgumentCaptor.forClass(PasswordResetToken.class);
            ArgumentCaptor<PasswordResetEmailRequested> evtCap =
                    ArgumentCaptor.forClass(PasswordResetEmailRequested.class);

            requestPasswordResetService.requestReset(email);

            InOrder inOrder = inOrder(tokens, eventBus);
            inOrder.verify(tokens).deleteAllForUser(userId);
            inOrder.verify(tokens).save(tokenCap.capture());
            inOrder.verify(eventBus).publish(evtCap.capture());

            var evt = evtCap.getValue();
            assertEquals(email.getValue(), evt.email());
            assertTrue(evt.url().startsWith(TEST_BASE_URL + "?token="));

            String rawToken = evt.url().substring((TEST_BASE_URL + "?token=").length());
            assertFalse(rawToken.isBlank());

            PasswordResetToken saved = tokenCap.getValue();
            assertNotNull(saved.id());
            assertEquals(userId, saved.userId());
            assertNull(saved.usedAt());
            assertEquals(FIXED_NOW, saved.createdAt());
            assertEquals(FIXED_NOW.plus(TTL), saved.expiresAt());

            String expectedHash = sha256Base64(rawToken);
            assertEquals(expectedHash, saved.tokenHash());
        }

        @Test
        void whenUserNotFound_doNothingVisible() {
            when(users.findByEmail(IdentityEmail.of("missing@example.com"))).thenReturn(Optional.empty());

            requestPasswordResetService.requestReset(IdentityEmail.of("missing@example.com"));

            verify(tokens, never()).save(any());
            verify(tokens, never()).deleteAllForUser(any());
            verify(eventBus, never()).publish(any());
        }
    }

    @Nested
    class ResetPassword {

        @Test
        void withValidToken_updatesPassword_marksUsed_revokesSessions() {
            UserId userId = UserId.newId();
            String newPassword = "NewPass123!";
            String anyRawToken = "whatever-raw-token";

            String hashedNew = BCRYPT.encode(newPassword);

            PasswordResetToken token = new PasswordResetToken(
                    PasswordResetTokenId.newId(),
                    userId,
                    "HASHED_TOKEN",
                    FIXED_NOW.plus(30, ChronoUnit.MINUTES),
                    null,
                    FIXED_NOW
            );

            when(tokens.findValidByHash(anyString(), any(Instant.class)))
                    .thenReturn(Optional.of(token));
            when(passwordHasher.hash(newPassword)).thenReturn(hashedNew);

            resetPasswordService.reset(anyRawToken, newPassword);

            verify(passwordPolicy).validate(eq(newPassword));
            InOrder inOrder = inOrder(users, tokens, sessions);
            inOrder.verify(users).updatePasswordHash((userId), (hashedNew));
            inOrder.verify(tokens).markUsed(eq(token.id()), eq(FIXED_NOW));
            inOrder.verify(sessions).revokeAllSessions(eq(userId));
        }

        @Test
        void withInvalidToken_throws_andDoesNotMutate() {
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
            UserId userId = UserId.newId();
            String currentPlaintext = "Old123!";
            PasswordHash currentHash = hashOf(currentPlaintext);
            String nextPlaintext = "NewPass123!";
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
        void withWrongCurrent_throwsInvalidCurrent_andDoesNotUpdate() {
            UserId userId = UserId.newId();
            String current = "Wrong!";
            PasswordHash storedHash = hashOf("Old123!");
            String next = "NewPass123!";

            CredentialsView userView =
                    new CredentialsView(userId, IdentityEmail.of("u@example.com"), storedHash, true);

            when(users.findById(userId)).thenReturn(Optional.of(userView));
            when(passwordHasher.matches(current, storedHash.getValue())).thenReturn(false);

            assertThrows(InvalidPasswordException.class,
                    () -> changePasswordService.change(userId, current, next));

            verify(users, never()).updatePasswordHash(any(), any());
            verify(sessions, never()).revokeAllSessions(any());
        }

        @Test
        void withSameNewPassword_throwsNewPasswordSameAsOld() {
            UserId userId = UserId.newId();
            String current = "Equal123!";
            PasswordHash storedHash = hashOf(current);
            String next = "Equal123!";

            CredentialsView cw =
                    new CredentialsView(userId, IdentityEmail.of("u@example.com"), storedHash, true);

            when(users.findById(userId)).thenReturn(Optional.of(cw));
            when(passwordHasher.matches(current, storedHash.getValue())).thenReturn(true);
            when(passwordHasher.matches(next, storedHash.getValue())).thenReturn(true);

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
