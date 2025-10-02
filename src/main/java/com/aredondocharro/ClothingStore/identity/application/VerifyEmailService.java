package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.domain.exception.VerificationTokenInvalidException;
import com.aredondocharro.ClothingStore.identity.domain.model.RefreshSession;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.in.VerifyEmailUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.LoadUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.RefreshTokenStorePort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SaveUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenGeneratorPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenVerifierPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.VerificationTokenPort;
import com.aredondocharro.ClothingStore.shared.log.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class VerifyEmailService implements VerifyEmailUseCase {

    private final VerificationTokenPort verifier;
    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;
    private final TokenGeneratorPort tokens;
    private final RefreshTokenStorePort refreshStore;
    private final TokenVerifierPort tokenVerifier;

    @Override
    public AuthResult verify(String verificationToken) {
        UUID userId = verifier.validateAndExtractUserId(verificationToken);
        User user = loadUserPort.findById(userId)
                .orElseThrow(() -> new VerificationTokenInvalidException("Invalid token"));

        if (!user.emailVerified()) {
            user = saveUserPort.save(user.verified());
            log.info("User email verified id={} email={}", user.id(), LogSanitizer.maskEmail(user.email().getValue()));
        } else {
            log.debug("User already verified id={}", user.id());
        }

        String accessToken = tokens.generateAccessToken(user);
        String refreshToken = tokens.generateRefreshToken(user);

        TokenVerifierPort.DecodedToken decoded = tokenVerifier.verify(refreshToken, "refresh");
        RefreshSession session = RefreshSession.issued(
                decoded.jti(),
                user.id(),
                decoded.expiresAt(),
                null,
                null
        );
        refreshStore.saveNew(session, refreshToken);

        return new AuthResult(accessToken, refreshToken);
    }
}
