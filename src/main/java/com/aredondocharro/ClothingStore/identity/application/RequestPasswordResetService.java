package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.domain.port.in.RequestPasswordResetUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.MailerPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordResetTokenRepositoryPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.UserRepositoryPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.view.CredentialsView;
import com.aredondocharro.ClothingStore.identity.domain.port.out.view.UserView;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

public class RequestPasswordResetService implements RequestPasswordResetUseCase {

    private final UserRepositoryPort users;
    private final PasswordResetTokenRepositoryPort tokens;
    private final MailerPort mailer;
    private final String resetBaseUrl;

    private static final SecureRandom RNG = new SecureRandom();
    private static final int TOKEN_BYTES = 32;   // ~256 bits
    private static final int EXP_MINUTES = 30;   // TTL 30 min

    public RequestPasswordResetService(UserRepositoryPort users,
                                       PasswordResetTokenRepositoryPort tokens,
                                       MailerPort mailer,
                                       String resetBaseUrl) {
        this.users = users;
        this.tokens = tokens;
        this.mailer = mailer;
        this.resetBaseUrl = resetBaseUrl;
    }

    @Override
    @Transactional
    public void requestReset(String email) {
        Optional<CredentialsView> userOpt = users.findByEmail(email);

        if (userOpt.isPresent()) {
            CredentialsView user = userOpt.get();

            tokens.deleteAllForUser(user.id());

            String rawToken = generateUrlSafeToken(TOKEN_BYTES);
            String tokenHash = sha256(rawToken);
            Instant expires = Instant.now().plus(EXP_MINUTES, ChronoUnit.MINUTES);

            PasswordResetTokenRepositoryPort.Token token =
                    new PasswordResetTokenRepositoryPort.Token(
                            UUID.randomUUID(), user.id(), tokenHash, expires, null, Instant.now()
                    );
            tokens.save(token);

            String link = buildResetLink(rawToken);
            mailer.sendPasswordResetLink(user.email().getValue(), link);
        }
        // 202 Accepted siempre en el controlador (anti-enumeración)
    }

    private static String generateUrlSafeToken(int numBytes) {
        byte[] buf = new byte[numBytes];
        RNG.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private String buildResetLink(String rawToken) {
        return resetBaseUrl + "?token=" + rawToken;
    }
}
