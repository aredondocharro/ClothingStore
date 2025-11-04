package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.contracts.event.PasswordResetEmailRequested;
import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordResetToken;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordResetTokenId;
import com.aredondocharro.ClothingStore.identity.domain.port.in.RequestPasswordResetUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordResetTokenRepositoryPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.UserRepositoryPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.view.CredentialsView;
import com.aredondocharro.ClothingStore.shared.domain.event.EventBusPort;
import com.aredondocharro.ClothingStore.shared.log.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private final EventBusPort eventBus;     // Notification enviará el email
    private final String resetBaseUrl;
    private final Clock clock;
    private final Duration ttl;

    private static final SecureRandom RNG = new SecureRandom();
    private static final int TOKEN_BYTES = 32; // ~256 bits

    @Override
    public void requestReset(IdentityEmail email) {
        log.debug("[FORGOT] request for {}", LogSanitizer.maskEmail(email.getValue()));

        Optional<CredentialsView> userOpt = users.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Anti-enumeration: comportamiento indistinguible
            log.info("[FORGOT] email not found (anti-enumeration): {}", LogSanitizer.maskEmail(email.getValue()));
            return;
        }

        CredentialsView user = userOpt.get();
        log.debug("[FORGOT] user found id={} email={}", user.id(), LogSanitizer.maskEmail(user.email().getValue()));

        // 1) Invalida tokens anteriores del usuario
        tokens.deleteAllForUser(user.id());

        // 2) Genera token raw (URL-safe) y calcula su hash (Base64 estándar) para almacenar
        String rawToken = generateUrlSafeToken(TOKEN_BYTES);

        // ⚠️ SOLO PARA DEBUG LOCAL (elimínalo en prod)
        log.warn("[TMP] RESET TOKEN => {}", rawToken);

        String tokenHash = sha256(rawToken);
        Instant now = Instant.now(clock);
        Instant expires = now.plus(ttl);

        // 3) Persiste el token de dominio
        tokens.save(new PasswordResetToken(
                PasswordResetTokenId.newId(),
                user.id(),
                tokenHash,
                expires,
                null,
                now
        ));

        // 4) Publica el evento con el enlace
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

    /** SHA-256 → Base64 estándar (no URL-safe), debe coincidir con el adapter/repositorio. */
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
