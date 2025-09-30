package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.domain.exception.EmailAlreadyExistException;
import com.aredondocharro.ClothingStore.identity.domain.exception.PasswordRequiredException;
import com.aredondocharro.ClothingStore.identity.domain.model.Email;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordHash;
import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.in.RegisterUserUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.LoadUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.MailerPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordHasherPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SaveUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenGeneratorPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.aredondocharro.ClothingStore.shared.log.LogSanitizer;

import java.util.Set;

import static com.aredondocharro.ClothingStore.shared.log.LogSanitizer.maskEmail;

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
    public AuthResult register(Email email, String rawPassword) {
        // Validación básica de entrada (defensa extra además del DTO)
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new PasswordRequiredException();
        }

        log.debug("Attempting to register user email={}", email.getValue());

        // Usuario ya existe → 409
        loadUserPort.findByEmail(email).ifPresent(u -> {
            log.warn("Registration rejected: already exists email={}", maskEmail(email.getValue()));
            throw new EmailAlreadyExistException();
        });

        // Hash + VO PasswordHash
        final String hashed = hasher.hash(rawPassword);
        final PasswordHash hashVO = PasswordHash.ofHashed(hashed);

        // Crear y guardar usuario (no verificado todavía)
        User toSave = new User(
                null,
                email,
                hashVO,
                false,
                Set.of(Role.USER),
                null
        );
        User saved = saveUserPort.save(toSave);

        // Generar token de verificación y enviar email
        String verification = tokens.generateVerificationToken(saved);
        String url = apiBaseUrl + "/auth/verify?token=" + verification;
        mailer.sendVerificationEmail(saved.email().getValue(), url);

        log.info("User registered id={} email={}", saved.id(), maskEmail(saved.email().getValue()));

        // No auto-login: devolvemos AuthResult vacío (puedes cambiar esto si lo deseas)
        return new AuthResult(null, null);
    }
}
