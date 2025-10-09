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

@Slf4j
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private final LoadUserPort loadUserPort;
    private final PasswordHasherPort hasher;
    private final TokenGeneratorPort tokens;
    private final RefreshTokenStorePort refreshStore;
    private final TokenVerifierPort tokenVerifier;

    @Override
    public AuthResult login(IdentityEmail email, String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new PasswordRequiredException();
        }

        log.debug("Login attempt email={}", email.getValue());

        User user = loadUserPort.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!hasher.matches(rawPassword, user.passwordHash().getValue())) {
            log.warn("Login failed email (Bad password) ={}", LogSanitizer.maskEmail(email.getValue()));
            throw new InvalidCredentialsException();
        }

        if (!user.emailVerified()) {
            throw new EmailNotVerifiedException();
        }

        String accessToken = tokens.generateAccessToken(user);
        String refreshToken = tokens.generateRefreshToken(user);

        // Decodifica el refresh recién emitido y persiste la sesión (jti/exp).
        TokenVerifierPort.DecodedToken decoded = tokenVerifier.verify(refreshToken, "refresh");
        RefreshSession session = RefreshSession.issued(
                decoded.jti(),           // jti
                user.id(),               // userId
                decoded.expiresAt(),     // expiresAt
                null,                    // ip (si la quieres guardar, pásala desde el controller)
                null                     // userAgent
        );
        refreshStore.saveNew(session, refreshToken);

        log.info("Login success email={}", LogSanitizer.maskEmail(email.getValue()));
        return new AuthResult(accessToken, refreshToken);
    }
}
