// src/main/java/com/aredondocharro/ClothingStore/identity/application/RegisterUserService.java
package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.domain.exception.EmailAlreadyExistException;
import com.aredondocharro.ClothingStore.identity.domain.exception.PasswordMismatchException;
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
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordPolicyPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SaveUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenGeneratorPort;
import com.aredondocharro.ClothingStore.shared.log.LogSanitizer;
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
    private final String verifyBaseUrl;
    private final PasswordPolicyPort passwordPolicy;

    @Override
    public AuthResult register(Email email, String rawPassword, String confirmPassword) {
        // 0) Precondiciones mínimas
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new PasswordRequiredException();
        }

        // 1) Política de contraseña (una sola fuente de verdad)
        passwordPolicy.validate(rawPassword);

        // 2) Coincidencia (si confirm viene null/blanco → mismatch)
        if (confirmPassword == null || !rawPassword.equals(confirmPassword)) {
            throw new PasswordMismatchException();
        }

        // 3) Unicidad del email
        loadUserPort.findByEmail(email).ifPresent(u -> { throw new EmailAlreadyExistException(); });

        // 4) Hash
        String bcrypt = hasher.hash(rawPassword);
        PasswordHash hashVO = PasswordHash.ofHashed(bcrypt);

        // 5) Construir y persistir
        User toSave = new User(
                null,              // id (lo setea la persistencia)
                email,
                hashVO,
                false,             // emailVerified
                Set.of(Role.USER), // roles iniciales
                null               // createdAt (si lo gestionas en DB)
        );
        User saved = saveUserPort.save(toSave);

        // 6) Token de verificación + enviar email
        String verification = tokens.generateVerificationToken(saved);
        String url = verifyBaseUrl + "?token=" + verification;
        mailer.sendVerificationEmail(saved.email().getValue(), url);

        log.info("User registered id={} email={}", saved.id(), LogSanitizer.maskEmail(saved.email().getValue()));

        // 7) Sin autologin en register
        return new AuthResult(null, null);
    }
}
