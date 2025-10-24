package com.aredondocharro.ClothingStore.identityTEST.application;

import com.aredondocharro.ClothingStore.identity.application.DeleteUserService;
import com.aredondocharro.ClothingStore.identity.domain.exception.UserNotFoundException;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SessionManagerPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.UserAdminRepositoryPort;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeleteUserServiceTest {

    @Test
    void whenFound_deletesAndRevokesSessions() {
        var repo = mock(UserAdminRepositoryPort.class);
        var sessions = mock(SessionManagerPort.class);
        var svc = new DeleteUserService(repo, sessions);

        UserId id = UserId.newId();
        when(repo.deleteById(id)).thenReturn(true);

        svc.delete(id);

        verify(repo).deleteById(id);
        verify(sessions).revokeAllSessions(id);
    }

    @Test
    void whenNotFound_throwsAndDoesNotRevoke() {
        var repo = mock(UserAdminRepositoryPort.class);
        var sessions = mock(SessionManagerPort.class);
        var svc = new DeleteUserService(repo, sessions);

        UserId id = UserId.newId();
        when(repo.deleteById(id)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> svc.delete(id));
        verify(sessions, never()).revokeAllSessions(any());
    }
}
