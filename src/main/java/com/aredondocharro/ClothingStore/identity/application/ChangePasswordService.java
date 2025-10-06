package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.domain.port.in.ChangePasswordUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordHasherPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordPolicyPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SessionManagerPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.UserRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class ChangePasswordService implements ChangePasswordUseCase {

    private final UserRepositoryPort users;
    private final PasswordHasherPort passwordHasher;
    private final PasswordPolicyPort passwordPolicy;
    private final SessionManagerPort sessions;

    public ChangePasswordService(UserRepositoryPort users,
                                 PasswordHasherPort passwordHasher,
                                 PasswordPolicyPort passwordPolicy,
                                 SessionManagerPort sessions) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.passwordPolicy = passwordPolicy;
        this.sessions = sessions;
    }

    @Override
    @Transactional
    public void change(UUID userId, String currentPassword, String newPassword) {
        UserRepositoryPort.UserView user = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean matches = passwordHasher.matches(currentPassword, user.passwordHash());
        if (!matches) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        passwordPolicy.validate(newPassword);

        String newHash = passwordHasher.hash(newPassword);
        users.updatePasswordHash(user.id(), newHash);

        sessions.revokeAllSessions(user.id());
    }
}
