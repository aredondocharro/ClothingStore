package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.in.LoginUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.LoadUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordHasherPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenGeneratorPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private final LoadUserPort loadUserPort;
    private final PasswordHasherPort hasher;
    private final TokenGeneratorPort tokens;

    @Override
    public AuthResult login(String email, String rawPassword) {
        log.debug("Login attempt email={}", email);
        User user = loadUserPort.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!hasher.matches(rawPassword, user.getPasswordHash())) {
            log.warn("Login failed (bad password) email={}", email);
            throw new IllegalArgumentException("Invalid credentials");
        }

        if (!user.isEmailVerified())
            throw new IllegalStateException("Email not verified"); // devuelve 403/401 desde un @ControllerAdvice

        log.info("Login success email={}", email);
        return new AuthResult(
                tokens.generateAccessToken(user),
                tokens.generateRefreshToken(user)
        );
    }
}
