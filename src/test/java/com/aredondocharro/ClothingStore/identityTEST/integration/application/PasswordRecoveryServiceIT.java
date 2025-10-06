package com.aredondocharro.ClothingStore.identityTEST.integration.application;

import com.aredondocharro.ClothingStore.TestcontainersConfiguration;
import com.aredondocharro.ClothingStore.identity.application.RequestPasswordResetService;
import com.aredondocharro.ClothingStore.identity.application.ResetPasswordService;
import com.aredondocharro.ClothingStore.identity.domain.port.in.RequestPasswordResetUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.ResetPasswordUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.MailerPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordHasherPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordPolicyPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordResetTokenRepositoryPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SessionManagerPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.UserRepositoryPort;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.PasswordResetTokenRepositoryAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringPasswordResetTokenJpa;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@TestPropertySource(properties = "app.reset.baseUrl=https://it.example/reset-password")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        TestcontainersConfiguration.class,
        PasswordResetTokenRepositoryAdapter.class,
        PasswordRecoveryServiceIT.TestBeans.class // <<-- Beans de los nuevos servicios
})
class PasswordRecoveryServiceIT {

    private static final String TEST_BASE_URL = "https://it.example/reset-password";

    @MockBean private UserRepositoryPort users;
    @MockBean private PasswordPolicyPort passwordPolicy;
    @MockBean private SessionManagerPort sessions;
    @MockBean private PasswordHasherPort passwordHasher;
    @MockBean private MailerPort mailer;

    @Autowired private RequestPasswordResetUseCase requestPasswordResetService;
    @Autowired private ResetPasswordUseCase resetPasswordService;

    @Autowired private SpringPasswordResetTokenJpa jpa;

    @Test
    void requestReset_persistsToken_andEmailsLink() throws Exception {
        // arrange
        UUID userId = UUID.randomUUID();
        String email = "u@example.com";
        when(users.findByEmail(email)).thenReturn(
                Optional.of(new UserRepositoryPort.UserView(userId, email, "hash", true))
        );

        ArgumentCaptor<String> emailCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> linkCap = ArgumentCaptor.forClass(String.class);

        // act
        requestPasswordResetService.requestReset(email);

        // assert (email enviado)
        verify(mailer).sendPasswordResetLink(emailCap.capture(), linkCap.capture());
        assertEquals(email, emailCap.getValue());
        String link = linkCap.getValue();
        assertTrue(link.startsWith(TEST_BASE_URL), "Reset link should start with baseUrl");

        // extrae el query param 'token'
        String rawTokenParam = getQueryParam(link, "token");
        assertNotNull(rawTokenParam, "token query param must exist");
        assertFalse(rawTokenParam.isBlank(), "token query param must not be blank");

        // fuerza flush por si el insert quedó pendiente
        jpa.flush();

        // verifica que hay un token válido para ese usuario (sin duplicar la lógica de hash)
        Instant now = Instant.now();
        var all = jpa.findAll();
        boolean hasValidTokenForUser = all.stream().anyMatch(t ->
                userId.equals(t.getUserId())
                        && t.getUsedAt() == null
                        && t.getExpiresAt().isAfter(now)
        );

        assertTrue(hasValidTokenForUser, "Token should be stored for the user and be valid (unused and not expired)");

        String rawToken = URLDecoder.decode(rawTokenParam, StandardCharsets.UTF_8);
        String b64 = sha256Base64(rawToken);
        String b64Url = sha256Base64UrlNoPad(rawToken);
        String hex = sha256Hex(rawToken);

        boolean byHashMatches =
                jpa.findByTokenHashAndExpiresAtAfterAndUsedAtIsNull(b64, now).isPresent()
                        || jpa.findByTokenHashAndExpiresAtAfterAndUsedAtIsNull(b64Url, now).isPresent()
                        || jpa.findByTokenHashAndExpiresAtAfterAndUsedAtIsNull(hex, now).isPresent();

        assertTrue(byHashMatches, "DB should contain a valid token matching the link's token hash");
    }

    @Test
    void reset_updatesPassword_marksUsed_revokes() {
        UUID userId = UUID.randomUUID();
        String rawToken = "ANY";
        String newPassword = "NewPass123!";
        String newHash = "HASHED";

        String tokenHash = sha256Base64(rawToken); // mismo formato que el servicio
        PasswordResetTokenRepositoryPort.Token token =
                new PasswordResetTokenRepositoryPort.Token(
                        UUID.randomUUID(), userId, tokenHash,
                        Instant.now().plusSeconds(1800), null, Instant.now()
                );

        // Persistimos token real en DB (adapter real)
        PasswordResetTokenRepositoryAdapter realAdapter = new PasswordResetTokenRepositoryAdapter(jpa);
        realAdapter.save(token);

        when(users.findById(userId)).thenReturn(Optional.of(new UserRepositoryPort.UserView(
                userId, "u@example.com", "OLD", true
        )));
        when(passwordHasher.hash(newPassword)).thenReturn(newHash);

        resetPasswordService.reset(rawToken, newPassword);

        verify(passwordPolicy).validate(eq(newPassword));
        verify(users).updatePasswordHash(eq(userId), eq(newHash));
        verify(sessions).revokeAllSessions(eq(userId));

        // token ya no es válido tras marcar used
        assertTrue(
                jpa.findByTokenHashAndExpiresAtAfterAndUsedAtIsNull(tokenHash, Instant.now()).isEmpty()
        );
    }

    // ----------------- Beans de test (wiring de servicios refactorizados) -----------------
    @TestConfiguration
    static class TestBeans {

        @Bean
        RequestPasswordResetUseCase requestPasswordResetUseCase(UserRepositoryPort users,
                                                                PasswordResetTokenRepositoryPort tokens,
                                                                MailerPort mailer,
                                                                @Value("${app.reset.baseUrl}") String baseUrl) {
            return new RequestPasswordResetService(users, tokens, mailer, baseUrl);
        }

        @Bean
        ResetPasswordUseCase resetPasswordUseCase(PasswordResetTokenRepositoryPort tokens,
                                                  PasswordPolicyPort passwordPolicy,
                                                  UserRepositoryPort users,
                                                  PasswordHasherPort passwordHasher,
                                                  SessionManagerPort sessions) {
            return new ResetPasswordService(tokens, passwordPolicy, users, passwordHasher, sessions);
        }
    }

    // ----------------- helpers -----------------

    private static String getQueryParam(String url, String name) {
        var uri = URI.create(url);
        String query = uri.getRawQuery();
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(name)) {
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private static String sha256Base64(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String sha256Base64UrlNoPad(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String sha256Hex(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
