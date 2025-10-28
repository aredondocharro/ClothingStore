package com.aredondocharro.ClothingStore.identityTEST.application;

import com.aredondocharro.ClothingStore.identity.application.RegisterUserService;
import com.aredondocharro.ClothingStore.identity.contracts.event.UserRegistered;
import com.aredondocharro.ClothingStore.identity.domain.model.*;
import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.out.*;
import com.aredondocharro.ClothingStore.shared.domain.event.EventBusPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterPublishesEventTest {

    @Mock LoadUserPort loadUsers;
    @Mock SaveUserPort saveUsers;
    @Mock PasswordHasherPort hasher;
    @Mock PasswordPolicyPort passwordPolicy;
    @Mock EventBusPort eventBus;

    private Clock clock;
    private RegisterUserService sut;

    @BeforeEach
    void setup() {
        clock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);
        sut = new RegisterUserService(loadUsers, saveUsers, hasher, passwordPolicy, clock, eventBus);
    }

    @Test
    void register_publishes_UserRegistered_with_expected_payload() {
        IdentityEmail email = IdentityEmail.of("john@example.com");

        // No existe usuario con ese email
        when(loadUsers.findByEmail(email)).thenReturn(Optional.empty());
        // Devolvemos un bcrypt "válido" para pasar la validación del VO (patrón regex)
        when(hasher.hash(anyString())).thenReturn("$2a$10$" + "A".repeat(53));
        // Guardar devuelve el propio agregado que recibe
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        when(saveUsers.save(saved.capture())).thenAnswer(inv -> inv.getArgument(0));

        AuthResult result = sut.register(email, "Str0ngPass!", "Str0ngPass!");

        // Publicación de evento
        ArgumentCaptor<UserRegistered> ev = ArgumentCaptor.forClass(UserRegistered.class);
        verify(eventBus).publish(ev.capture());

        // Assertions del evento
        assertThat(ev.getValue().occurredAt()).isEqualTo(Instant.now(clock));
        // userId del evento coincide con el del agregado persistido
        assertThat(ev.getValue().userId()).isEqualTo(saved.getValue().id().value());

        // El caso actual devuelve tokens nulos (según tu implementación)
        assertThat(result.accessToken()).isNull();
        assertThat(result.refreshToken()).isNull();

        // Validación de política de password fue invocada
        verify(passwordPolicy).validate("Str0ngPass!");
    }
}
