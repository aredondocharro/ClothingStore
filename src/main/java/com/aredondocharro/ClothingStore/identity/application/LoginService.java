package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.domain.exception.EmailNotVerifiedException;
import com.aredondocharro.ClothingStore.identity.domain.exception.InvalidCredentialsException;
import com.aredondocharro.ClothingStore.identity.domain.exception.PasswordRequiredException;
import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.model.RefreshSession;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.in.LoginUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.LoadUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordHasherPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.RefreshTokenStorePort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenGeneratorPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenVerifierPort;
import com.aredondocharro.ClothingStore.shared.log.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private final LoadUserPort loadUserPort;
    private final PasswordHasherPort hasher;
    private final TokenGeneratorPort tokens;
    private final RefreshTokenStorePort refreshStore;
    private final TokenVerifierPort tokenVerifier;
    private final Clock clock;

    @Override
    public AuthResult login(IdentityEmail email, String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) throw new PasswordRequiredException();

        log.debug("Login attempt email={}", email.getValue());

        User user = loadUserPort.findByEmail(email).orElseThrow(() -> {
            log.warn("Login failed email (User not found)={}", LogSanitizer.maskEmail(email.getValue()));
            return new InvalidCredentialsException("Wrong email or email not found");
        });

        if (!hasher.matches(rawPassword, user.passwordHash().getValue())) {
            log.warn("Login failed email (Bad password) ={}", LogSanitizer.maskEmail(email.getValue()));
            throw new InvalidCredentialsException("Wrong password");
        }
        if (!user.emailVerified()) throw new EmailNotVerifiedException();

        String accessToken  = tokens.generateAccessToken(user);
        String refreshToken = tokens.generateRefreshToken(user);

        // Decodifica el refresh y persiste la sesión
        TokenVerifierPort.DecodedToken decoded = tokenVerifier.verify(refreshToken, "refresh");

        // Fallback seguro para createdAt (iat puede no venir)

        RefreshSession session = RefreshSession.issue(
                decoded.jti(),
                user.id(),
                Instant.now(clock),
                decoded.expiresAt(),
                null, null
        );
        refreshStore.saveNew(session, refreshToken);

        log.info("Login success email={}", LogSanitizer.maskEmail(email.getValue()));
        return new AuthResult(accessToken, refreshToken);
    }
}
