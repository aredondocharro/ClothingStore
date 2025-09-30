package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.domain.exception.EmailNotVerifiedException;
import com.aredondocharro.ClothingStore.identity.domain.exception.InvalidCredentialsException;
import com.aredondocharro.ClothingStore.identity.domain.exception.PasswordRequiredException;
import com.aredondocharro.ClothingStore.identity.domain.model.Email;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.in.LoginUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.LoadUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordHasherPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenGeneratorPort;
import com.aredondocharro.ClothingStore.shared.log.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private final LoadUserPort loadUserPort;
    private final PasswordHasherPort hasher;
    private final TokenGeneratorPort tokens;

    @Override
    public AuthResult login(Email email, String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) throw new PasswordRequiredException();

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

        log.info("Login success email={}", LogSanitizer.askEmail(email.getValue()));
        return new AuthResult(tokens.generateAccessToken(user), tokens.generateRefreshToken(user));
    }

}
