package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.contracts.event.PasswordResetEmailRequested;
import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordResetTokenId;
import com.aredondocharro.ClothingStore.identity.domain.port.in.RequestPasswordResetUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordResetTokenRepositoryPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.UserRepositoryPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.view.CredentialsView;
import com.aredondocharro.ClothingStore.shared.domain.event.EventBusPort;
import com.aredondocharro.ClothingStore.shared.log.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
public class RequestPasswordResetService implements RequestPasswordResetUseCase {

    private final UserRepositoryPort users;
    private final PasswordResetTokenRepositoryPort tokens;
    private final EventBusPort eventBus;     // ← ya no MailerPort (Notification enviará el email)
    private final String resetBaseUrl;
    private final Clock clock;
    private final Duration ttl;              // ← configurable (p.ej. PT30M)

    private static final SecureRandom RNG = new SecureRandom();
    private static final int TOKEN_BYTES = 32; // ~256 bits


    @Override
    @Transactional
    public void requestReset(IdentityEmail email) {
        log.debug("[FORGOT] request for {}",LogSanitizer.maskEmail(email.getValue()));

        Optional<CredentialsView> userOpt = users.findByEmail(email);

        if (userOpt.isEmpty()) {
            log.info("[FORGOT] email not found (anti-enumeration): {}", LogSanitizer.maskEmail(email.getValue()));
            return;
        }

        CredentialsView user = userOpt.get();
        log.debug("[FORGOT] user found id={} email={}", user.id(), LogSanitizer.maskEmail(user.email().getValue()));


        tokens.deleteAllForUser(user.id());
        String rawToken = generateUrlSafeToken(TOKEN_BYTES);

        //MUY IMPORTANTE: BORRAR ESTO ANTES DE PRODUCCIÓN -SOLO PARA TESTING Y DEBUGGING-
        log.warn("[TMP] RESET TOKEN => {}", rawToken);

        String tokenHash = sha256(rawToken);
        Instant now = Instant.now(clock);
        Instant expires = now.plus(ttl);
        tokens.save(new PasswordResetTokenRepositoryPort.Token(
                PasswordResetTokenId.newId(), user.id(), tokenHash, expires, null, now
        ));
        String link = resetBaseUrl + "?token=" + rawToken;
        eventBus.publish(new PasswordResetEmailRequested(user.email().getValue(), link, now));
        log.info("[FORGOT] published PasswordResetEmailRequested to {}", LogSanitizer.maskEmail(user.email().getValue()));
    }

    /* ===================== helpers ===================== */

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
}
