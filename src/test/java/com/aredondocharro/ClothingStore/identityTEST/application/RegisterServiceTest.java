package com.aredondocharro.ClothingStore.identityTEST.application;

import com.aredondocharro.ClothingStore.identity.application.RegisterUserService;
import com.aredondocharro.ClothingStore.identity.contracts.event.UserRegistered;
import com.aredondocharro.ClothingStore.identity.domain.exception.EmailAlreadyExistException;
import com.aredondocharro.ClothingStore.identity.domain.exception.PasswordMismatchException;
import com.aredondocharro.ClothingStore.identity.domain.exception.PasswordRequiredException;
import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.out.LoadUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordHasherPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordPolicyPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SaveUserPort;
import com.aredondocharro.ClothingStore.shared.domain.event.EventBusPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {

    @Mock LoadUserPort loadUserPort;
    @Mock SaveUserPort saveUserPort;
    @Mock PasswordHasherPort hasher;
    @Mock PasswordPolicyPort passwordPolicy;
    @Mock EventBusPort eventBus;

    RegisterUserService service;

    // clock fijo para tests deterministas
    private static final Instant FIXED_NOW = Instant.parse("2025-01-01T00:00:00Z");
    private final Clock clock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        service = new RegisterUserService(
                loadUserPort, saveUserPort, hasher, passwordPolicy, clock, eventBus
        );
    }

    @Test
    void register_nullOrBlankPassword_throwsPasswordRequired_andDoesNotHitPorts() {
        IdentityEmail email = IdentityEmail.of("user@example.com");

        assertAll(
                () -> assertThrows(PasswordRequiredException.class, () -> service.register(email, null, "x")),
                () -> assertThrows(PasswordRequiredException.class, () -> service.register(email, "", "x")),
                () -> assertThrows(PasswordRequiredException.class, () -> service.register(email, "   ", "x"))
        );

        verifyNoInteractions(loadUserPort, hasher, saveUserPort, passwordPolicy, eventBus);
    }

    @Test
    void register_passwordsDoNotMatch_throwsPasswordMismatch_andDoesNotHashOrSave() {
        IdentityEmail email = IdentityEmail.of("user@example.com");
        String raw = "Secret123!";
        String confirm = "Other123!";

        assertThrows(PasswordMismatchException.class, () -> service.register(email, raw, confirm));

        verify(passwordPolicy).validate(raw);
        verifyNoInteractions(loadUserPort, hasher, saveUserPort, eventBus);
    }

    @Test
    void register_existingEmail_throwsEmailAlreadyExistException_andDoesNotHashOrSave() {
        IdentityEmail email = IdentityEmail.of("user@example.com");
        String raw = "Secret123!";

        when(loadUserPort.findByEmail(email)).thenReturn(Optional.of(mock(User.class)));

        assertThrows(EmailAlreadyExistException.class, () -> service.register(email, raw, raw));

        InOrder inOrder = inOrder(passwordPolicy, loadUserPort);
        inOrder.verify(passwordPolicy).validate(raw);
        inOrder.verify(loadUserPort).findByEmail(email);
        inOrder.verifyNoMoreInteractions();

        verifyNoInteractions(hasher, saveUserPort, eventBus);
    }

    @Test
    void register_success_hashes_saves_and_publishesEvent() {
        IdentityEmail email = IdentityEmail.of("user@example.com");
        String rawPassword = "Secret123!";

        when(loadUserPort.findByEmail(email)).thenReturn(Optional.empty());

        String bcrypt = "$2b$10$7EqJtq98hPqEX7fNZaFWoO5f.Pg3rQAYyu3iJ/T9Y2aXx1Z9E6iGa";
        when(hasher.hash(rawPassword)).thenReturn(bcrypt);

        when(saveUserPort.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0, User.class);
            // simulamos que “persiste y devuelve” lo mismo
            return u;
        });

        AuthResult result = service.register(email, rawPassword, rawPassword);

        assertNotNull(result);
        assertNull(result.accessToken());
        assertNull(result.refreshToken());

        InOrder inOrder = inOrder(passwordPolicy, loadUserPort, hasher, saveUserPort, eventBus);
        inOrder.verify(passwordPolicy).validate(rawPassword);
        inOrder.verify(loadUserPort).findByEmail(email);
        inOrder.verify(hasher).hash(rawPassword);
        inOrder.verify(saveUserPort).save(argThat(u ->
                u.email().equals(email)
                        && !u.emailVerified()
                        && u.roles().contains(Role.USER)
        ));

        // Solo verificamos que se publica el evento correcto
        ArgumentCaptor<UserRegistered> evtCap = ArgumentCaptor.forClass(UserRegistered.class);
        inOrder.verify(eventBus).publish(evtCap.capture());
        inOrder.verifyNoMoreInteractions();

        var evt = evtCap.getValue();
        assertNotNull(evt);
        // si tu evento es opción A (primitivos) adapta estos asserts:
        // assertEquals(email.getValue(), evt.email());
        // assertEquals(FIXED_NOW, evt.occurredAt());
        // si es opción B (VOs):
        // assertEquals(email, evt.email());
        // assertEquals(FIXED_NOW, evt.occurredAt());
    }
}
