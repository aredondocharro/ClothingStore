package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.domain.exception.UserNotFoundException;
import com.aredondocharro.ClothingStore.identity.domain.port.in.DeleteUserUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SessionManagerPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.UserAdminRepositoryPort;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@AllArgsConstructor
public class DeleteUserService implements DeleteUserUseCase {

    private final UserAdminRepositoryPort repo;
    private final SessionManagerPort sessions;


    @Override
    public void delete(UUID userId) {
        boolean existed = repo.deleteById(userId);
        if (!existed) throw new UserNotFoundException(userId);
        sessions.revokeAllSessions(userId);
        log.warn("User deleted id={}", userId);
    }
}
