// src/main/java/com/aredondocharro/ClothingStore/identity/application/RegisterUserService.java
package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.in.RegisterUserUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;
    private final PasswordHasherPort hasher;
    private final TokenGeneratorPort tokens;
    private final MailerPort mailer;
    private final String apiBaseUrl;

    @Override
    public AuthResult register(String email, String rawPassword) {
        log.debug("Attempting to register user email={}", email);
        loadUserPort.findByEmail(email.toLowerCase()).ifPresent(u -> {
            log.warn("Registration rejected: already exists email={}", email);
            throw new IllegalStateException("Email already registered");
        });

        String hash = hasher.hash(rawPassword);
        User saved = saveUserPort.save(new User(null, email, hash, false, Set.of("USER"), null));

        String verification = tokens.generateVerificationToken(saved);
        String url = apiBaseUrl + "/auth/verify?token=" + verification;
        mailer.sendVerificationEmail(saved.getEmail(), url);

        log.info("User registered id={} email={}", saved.getId(), saved.getEmail());
        return new AuthResult(null, null); // sin tokens aún
    }
}
