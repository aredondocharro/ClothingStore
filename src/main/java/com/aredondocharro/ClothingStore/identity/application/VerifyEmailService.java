// src/main/java/.../VerifyEmailService.java
package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.domain.port.out.error.VerificationTokenInvalidException;
import com.aredondocharro.ClothingStore.identity.domain.model.RefreshSession;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.in.VerifyEmailUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.*;
import com.aredondocharro.ClothingStore.shared.log.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
public class VerifyEmailService implements VerifyEmailUseCase {
    private final VerificationTokenPort verifier;
    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;
    private final TokenGeneratorPort tokens;
    private final RefreshTokenStorePort refreshStore;
    private final RefreshTokenVerifierPort refreshVerifier;
    private final Clock clock;

    @Override
    public AuthResult verify(String verificationToken) {
        UUID userId = verifier.validateAndExtractUserId(verificationToken);
        User user = loadUserPort.findById(UserId.of(userId))
                .orElseThrow(() -> new VerificationTokenInvalidException("Invalid token"));

        if (!user.emailVerified()) {
            user = saveUserPort.save(user.verified());
            log.info("User email verified id={} email={}", user.id(), LogSanitizer.maskEmail(user.email().getValue()));
        }

        String accessToken  = tokens.generateAccessToken(user);
        String refreshToken = tokens.generateRefreshToken(user);

        var decoded = refreshVerifier.verify(refreshToken);

        // ⬇⬇ createdAt controlado por la app (no dependemos de iat)
        RefreshSession session = RefreshSession.issue(
                decoded.jti(),
                user.id(),
                Instant.now(clock),
                decoded.expiresAt(),
                null,
                null
        );
        refreshStore.saveNew(session, refreshToken);

        return new AuthResult(accessToken, refreshToken);
    }
}

