package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.domain.model.RefreshSession;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.in.VerifyEmailUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.*;
import com.aredondocharro.ClothingStore.identity.domain.port.out.error.VerificationTokenInvalidException;
import com.aredondocharro.ClothingStore.shared.log.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public class VerifyEmailService implements VerifyEmailUseCase {

    private final VerificationTokenPort verifier;
    private final VerificationTokenStorePort verificationTokenStore;
    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;
    private final TokenGeneratorPort tokens;
    private final RefreshTokenStorePort refreshStore;
    private final RefreshTokenVerifierPort refreshVerifier;
    private final Clock clock;

    @Override
    public AuthResult verify(String verificationToken) {
        Instant now = Instant.now(clock);

        // 1) Verificar JWT y extraer userId + jti
        VerificationTokenPort.VerificationTokenData data = verifier.validate(verificationToken);

        // 2) Comprobar si ese jti sigue activo en la tabla verification_tokens
        if (!verificationTokenStore.isTokenActive(data.jti())) {
            log.warn("Verification token used but is not active (jti={})", data.jti());
            throw new VerificationTokenInvalidException("Invalid or expired verification token");
        }

        // 3) Cargar usuario
        User user = loadUserPort.findById(UserId.of(data.userId()))
                .orElseThrow(() -> new VerificationTokenInvalidException("Invalid token"));

        // 4) Marcar email verificado si aún no lo está
        if (!user.emailVerified()) {
            user = saveUserPort.save(user.verified());
            log.info("User email verified id={} email={}",
                    user.id(), LogSanitizer.maskEmail(user.email().getValue()));
        }

        // 5) Consumir el token de verificación → single-use
        verificationTokenStore.revokeToken(data.jti(), now);
        log.info("Verification token consumed jti={} userId={}", data.jti(), user.id());

        // 6) Generar access + refresh tokens
        String accessToken  = tokens.generateAccessToken(user);
        String refreshToken = tokens.generateRefreshToken(user);

        var decoded = refreshVerifier.verify(refreshToken);

        // createdAt controlado por la app (no dependemos de iat)
        RefreshSession session = RefreshSession.issue(
                decoded.jti(),
                user.id(),
                now,
                decoded.expiresAt(),
                null,
                null
        );
        refreshStore.saveNew(session, refreshToken);

        return new AuthResult(accessToken, refreshToken);
    }
}
