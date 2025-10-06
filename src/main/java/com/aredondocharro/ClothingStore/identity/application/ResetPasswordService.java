package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.domain.port.in.ResetPasswordUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordHasherPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordPolicyPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordResetTokenRepositoryPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SessionManagerPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.UserRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

public class ResetPasswordService implements ResetPasswordUseCase {

    private final PasswordResetTokenRepositoryPort tokens;
    private final PasswordPolicyPort passwordPolicy;
    private final UserRepositoryPort users;
    private final PasswordHasherPort passwordHasher;
    private final SessionManagerPort sessions;

    public ResetPasswordService(PasswordResetTokenRepositoryPort tokens,
                                PasswordPolicyPort passwordPolicy,
                                UserRepositoryPort users,
                                PasswordHasherPort passwordHasher,
                                SessionManagerPort sessions) {
        this.tokens = tokens;
        this.passwordPolicy = passwordPolicy;
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.sessions = sessions;
    }

    @Override
    @Transactional
    public void reset(String rawToken, String newPassword) {
        String hash = sha256(rawToken);
        Instant now = Instant.now();

        PasswordResetTokenRepositoryPort.Token token = tokens.findValidByHash(hash, now)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

        passwordPolicy.validate(newPassword);

        String newHash = passwordHasher.hash(newPassword);
        users.updatePasswordHash(token.userId(), newHash);

        tokens.markUsed(token.id(), now);

        sessions.revokeAllSessions(token.userId());
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
