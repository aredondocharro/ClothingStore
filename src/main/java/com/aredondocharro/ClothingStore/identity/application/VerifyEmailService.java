package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.domain.exception.VerificationTokenInvalidException;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.in.VerifyEmailUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.LoadUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SaveUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenGeneratorPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.VerificationTokenPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class VerifyEmailService implements VerifyEmailUseCase {

    private final VerificationTokenPort verifier;
    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;
    private final TokenGeneratorPort tokens; // <- necesario para autologin

    @Override
    public AuthResult verify(String verificationToken) {
        UUID userId = verifier.validateAndExtractUserId(verificationToken);
        User user = loadUserPort.findById(userId)
                .orElseThrow(() -> new VerificationTokenInvalidException("Invalid token"));

        if (!user.emailVerified()) {
            user = saveUserPort.save(user.verified());
            log.info("User email verified id={} email={}", user.id(), user.email().getValue());
        } else {
            log.debug("User already verified id={}", user.id());
        }

        // autologin
        return new AuthResult(
                tokens.generateAccessToken(user),
                tokens.generateRefreshToken(user)
        );
    }
}
