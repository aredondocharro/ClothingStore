package com.aredondocharro.ClothingStore.identityTEST.application;

import com.aredondocharro.ClothingStore.identity.application.UpdateUserRolesService;
import com.aredondocharro.ClothingStore.identity.domain.exception.CannotRemoveLastAdminException;
import com.aredondocharro.ClothingStore.identity.domain.exception.UserNotFoundException;
import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.out.UserAdminRepositoryPort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateUserRolesServiceTest {

    private final UserAdminRepositoryPort repo = mock(UserAdminRepositoryPort.class);
    private final UpdateUserRolesService service = new UpdateUserRolesService(repo);

    @Test
    void whenUserNotFound_throws() {
        UserId uid = UserId.newId();
        when(repo.existsById(uid)).thenReturn(false);

        assertThrows(UserNotFoundException.class,
                () -> service.setRoles(uid, Set.of(Role.USER)));

        // Verifica la interacción esperada…
        verify(repo).existsById(uid);
        // …y que no hubo ninguna otra
        verifyNoMoreInteractions(repo);
    }

    @Test
    void removingLastAdmin_throws() {
        UserId uid = UserId.newId();
        when(repo.existsById(uid)).thenReturn(true);
        when(repo.hasRole(uid, Role.ADMIN)).thenReturn(true);
        when(repo.countUsersWithRole(Role.ADMIN)).thenReturn(1);

        assertThrows(CannotRemoveLastAdminException.class,
                () -> service.setRoles(uid, Set.of(Role.USER)));

        verify(repo, never()).updateRoles(any(), any());
    }

    @Test
    void emptyRoles_defaultsToUSER_andUpdates() {
        UserId uid = UserId.newId();
        when(repo.existsById(uid)).thenReturn(true);
        when(repo.hasRole(uid, Role.ADMIN)).thenReturn(false);

        service.setRoles(uid, Set.of()); // vacío -> debe normalizar a USER

        ArgumentCaptor<Set<Role>> cap = ArgumentCaptor.forClass(Set.class);
        verify(repo).updateRoles(eq(uid), cap.capture());
        assertEquals(Set.of(Role.USER), cap.getValue());
    }
}
