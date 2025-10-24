package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.domain.exception.*;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.in.ChangePasswordUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordHasherPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordPolicyPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SessionManagerPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.UserRepositoryPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.view.CredentialsView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j // Logs are in English and never include raw passwords or hashes
@RequiredArgsConstructor
public class ChangePasswordService implements ChangePasswordUseCase {

    private final UserRepositoryPort users;
    private final PasswordHasherPort passwordHasher;
    private final PasswordPolicyPort passwordPolicy;
    private final SessionManagerPort sessions;

    @Override
    @Transactional
    public void change(UserId userId, String currentPassword, String newPassword) {
        log.debug("Change password requested (userId={})", userId);

        // 1) Load credentials
        Optional<CredentialsView> maybeCreds = users.findById(userId);
        if (maybeCreds.isEmpty()) {
            log.warn("Change password aborted: user not found (userId={})", userId);
            throw new UserNotFoundException(userId);
        }
        CredentialsView creds = maybeCreds.get();
        log.debug("User credentials loaded (userId={})", userId);

        // 2) Verify current password
        if (!passwordHasher.matches(currentPassword, creds.passwordHash().getValue())) {
            log.warn("Current password does not match (userId={})", userId);
            throw new InvalidPasswordException("Password doesn't match");
        }
        log.debug("Current password verified (userId={})", userId);

        // 3) Enforce password policy (do not log the password)
        passwordPolicy.validate(newPassword);
        log.debug("New password passed policy validation (userId={})", userId);

        // 4) Prevent reusing the same password
        if (passwordHasher.matches(newPassword, creds.passwordHash().getValue())) {
            log.warn("New password is the same as the old one (userId={})", userId);
            throw new NewPasswordSameAsOldException();
        }

        // 5) Persist new hash
        String newHash = passwordHasher.hash(newPassword);
        users.updatePasswordHash(userId, newHash);
        log.info("Password hash updated successfully (userId={})", userId);

        // 6) Revoke active sessions for this user
        sessions.revokeAllSessions(userId);
        log.info("All sessions revoked after password change (userId={})", userId);
    }
}
