package com.aredondocharro.ClothingStore.identity.application;

import com.aredondocharro.ClothingStore.identity.domain.exception.UserNotFoundException;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.in.DeleteUserUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SessionManagerPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.UserAdminRepositoryPort;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class DeleteUserService implements DeleteUserUseCase {

    private final UserAdminRepositoryPort repo;
    private final SessionManagerPort sessions;


    @Override
    public void delete(UserId userId) {
        boolean existed = repo.deleteById(userId);
        if (!existed) throw new UserNotFoundException(userId);
        sessions.revokeAllSessions(userId);
        log.warn("User deleted id={}", userId);
    }
}
