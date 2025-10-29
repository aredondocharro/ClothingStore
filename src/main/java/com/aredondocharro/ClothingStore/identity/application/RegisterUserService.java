package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.contracts.event.UserRegistered;
import com.aredondocharro.ClothingStore.identity.domain.exception.EmailAlreadyExistException;
import com.aredondocharro.ClothingStore.identity.domain.exception.PasswordMismatchException;
import com.aredondocharro.ClothingStore.identity.domain.exception.PasswordRequiredException;
import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.model.PasswordHash;
import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.in.RegisterUserUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.LoadUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordHasherPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordPolicyPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SaveUserPort;

import com.aredondocharro.ClothingStore.shared.domain.event.EventBusPort;
import com.aredondocharro.ClothingStore.shared.log.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import java.time.Clock;
import java.time.Instant;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;
    private final PasswordHasherPort hasher;
    private final PasswordPolicyPort passwordPolicy;
    private final Clock clock;
    private final EventBusPort eventBus;

    @Override
    public AuthResult register(IdentityEmail email, String rawPassword, String confirmPassword) {
        if (rawPassword == null || rawPassword.isBlank()) throw new PasswordRequiredException();
        passwordPolicy.validate(rawPassword);
        if (confirmPassword == null || !rawPassword.equals(confirmPassword)) throw new PasswordMismatchException();
        loadUserPort.findByEmail(email).ifPresent(u -> { throw new EmailAlreadyExistException(); });

        String bcrypt = hasher.hash(rawPassword);
        PasswordHash hashVO = PasswordHash.ofHashed(bcrypt);

        UserId userId = UserId.newId();
        Instant now = Instant.now(clock);      // ← tiempo en aplicación

        // Factory with semantics (add this in User)
        User user = User.create(userId, email, hashVO, Set.of(Role.USER), now);
        User saved = saveUserPort.save(user);

        // (Direct email for now; replace with domain event when ready)
        eventBus.publish(new UserRegistered(userId.value(), now));

        log.info("PUBLISHED: User registered id={} email={}", saved.id(), LogSanitizer.maskEmail(saved.email().getValue()));
        return new AuthResult(null, null);
    }
}

