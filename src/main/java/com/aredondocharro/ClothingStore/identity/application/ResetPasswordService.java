package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.domain.exception.PasswordResetTokenInvalidException;
import com.aredondocharro.ClothingStore.identity.domain.port.in.ResetPasswordUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordHasherPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordPolicyPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordResetTokenRepositoryPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SessionManagerPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class ResetPasswordService implements ResetPasswordUseCase {

    private final PasswordResetTokenRepositoryPort tokens;
    private final PasswordPolicyPort passwordPolicy;
    private final UserRepositoryPort users;
    private final PasswordHasherPort passwordHasher;
    private final SessionManagerPort sessions;
    private final Clock clock;

    @Override
    @Transactional
    public void reset(String rawToken, String newPassword) {
        Instant now = Instant.now(clock);

        // Logs estables (no sensibles)
        log.debug("Password RESET invoked at={} token={}", now, mask(rawToken, 6));

        // 1) Hash del token recibido (mismo algoritmo que al guardar)
        String hash = sha256(rawToken);
        log.debug("Password RESET computed hash={}", mask(hash, 8));

        // 2) Buscar token válido (no usado, no expirado)
        Optional<PasswordResetTokenRepositoryPort.Token> tokenOpt = tokens.findValidByHash(hash, now);
        PasswordResetTokenRepositoryPort.Token token = tokenOpt.orElseThrow(PasswordResetTokenInvalidException::new);
        log.info("Password reset token accepted userId={} tokenId={}", token.userId(), token.id());

        // 3) Política de contraseña
        passwordPolicy.validate(newPassword);

        // 4) Persistir nuevo hash de contraseña
        String newHash = passwordHasher.hash(newPassword);
        users.updatePasswordHash(token.userId(), newHash);
        log.info("Password updated for userId={}", token.userId());

        // 5) Marcar token como usado (one-time)
        tokens.markUsed(token.id(), now);
        log.debug("Reset token marked as used tokenId={} at={}", token.id(), now);

        // 6) Revocar sesiones activas del usuario
        sessions.revokeAllSessions(token.userId());
        log.info("All sessions revoked for userId={}", token.userId());
    }

    /** SHA-256 del texto del token → Base64 estándar (44 chars con padding) */
    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes); // Encoder estándar, no URL-safe
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Enmascara valores sensibles para logs (muestra los primeros N chars y la longitud). */
    private static String mask(String value, int prefix) {
        if (value == null) return "null";
        int len = value.length();
        int keep = Math.min(Math.max(prefix, 0), len);
        return value.substring(0, keep) + "*** (len=" + len + ")";
    }
}
