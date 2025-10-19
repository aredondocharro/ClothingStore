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

@Slf4j //falta comentario
@RequiredArgsConstructor
public class ChangePasswordService implements ChangePasswordUseCase {

    private final UserRepositoryPort users;
    private final PasswordHasherPort passwordHasher;
    private final PasswordPolicyPort passwordPolicy;
    private final SessionManagerPort sessions;

    @Override
    @Transactional
    public void change(UserId userId, String currentPassword, String newPassword) {
        // 1) Cargar credenciales
        CredentialsView creds = users.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        // 2) Verificar contraseña actual (si falla -> excepción específica)
        if (!passwordHasher.matches(currentPassword, creds.passwordHash().getValue())) {
            throw new InvalidPasswordException("Password doesn't match");
        }
        // 3) Política de complejidad
        passwordPolicy.validate(newPassword);

        // 4) Evitar reutilizar la misma contraseña
        if (passwordHasher.matches(newPassword, creds.passwordHash().getValue())) {
            throw new NewPasswordSameAsOldException();
        }
        // 5) Persistir nuevo hash
        String newHash = passwordHasher.hash(newPassword);
        users.updatePasswordHash(userId, newHash);

        // 6) Revocar sesiones activas del usuario
        sessions.revokeAllSessions(userId);
    }
}
