package com.aredondocharro.ClothingStore.identityTEST.application;

import com.aredondocharro.ClothingStore.identity.application.RegisterUserService;
import com.aredondocharro.ClothingStore.identity.domain.exception.EmailAlreadyExistException;
import com.aredondocharro.ClothingStore.identity.domain.exception.PasswordRequiredException;
import com.aredondocharro.ClothingStore.identity.domain.model.Email;
import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.out.LoadUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.MailerPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordHasherPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SaveUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenGeneratorPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    @Mock TokenGeneratorPort tokens;
    @Mock MailerPort mailer;

    RegisterUserService service;

    final String apiBaseUrl = "https://api.example.com";

    @BeforeEach
    void setUp() {
        service = new RegisterUserService(loadUserPort, saveUserPort, hasher, tokens, mailer, apiBaseUrl);
    }

    @Test
    void register_nullOrBlankPassword_throwsPasswordRequired_andDoesNotHitPorts() {
        var email = Email.of("user@example.com");

        assertAll(
                () -> assertThrows(PasswordRequiredException.class, () -> service.register(email, null)),
                () -> assertThrows(PasswordRequiredException.class, () -> service.register(email, "")),
                () -> assertThrows(PasswordRequiredException.class, () -> service.register(email, "   "))
        );

        verifyNoInteractions(loadUserPort, hasher, saveUserPort, tokens, mailer);
    }

    @Test
    void register_existingEmail_throwsEmailAlreadyExistException_andDoesNotHashOrSave() {
        var email = Email.of("user@example.com");
        // simulamos que existe usuario
        when(loadUserPort.findByEmail(email)).thenReturn(Optional.of(mock(User.class)));

        assertThrows(EmailAlreadyExistException.class, () -> service.register(email, "Secret123!"));

        verify(loadUserPort).findByEmail(email);
        verifyNoMoreInteractions(loadUserPort);
        verifyNoInteractions(hasher, saveUserPort, tokens, mailer);
    }

    @Test
    void register_success_hashes_saves_generatesToken_and_sendsVerificationEmail() {
        var email = Email.of("user@example.com");
        var rawPassword = "Secret123!";

        when(loadUserPort.findByEmail(email)).thenReturn(Optional.empty());

        // Hash bcrypt válido (60 chars) para que pase PasswordHash.ofHashed(...)
        var bcrypt = "$2b$10$7EqJtq98hPqEX7fNZaFWoO5f.Pg3rQAYyu3iJ/T9Y2aXx1Z9E6iGa";
        when(hasher.hash(rawPassword)).thenReturn(bcrypt);

        // save devuelve el mismo User que recibe (para no depender del constructor exacto)
        when(saveUserPort.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0, User.class));

        var token = "verification-token-123";
        when(tokens.generateVerificationToken(any(User.class))).thenReturn(token);

        AuthResult result = service.register(email, rawPassword);

        // resultado (tu servicio devuelve null/null por diseño)
        assertNotNull(result);
        assertNull(result.accessToken());
        assertNull(result.refreshToken());

        // orden y llamadas
        InOrder inOrder = inOrder(loadUserPort, hasher, saveUserPort, tokens, mailer);
        inOrder.verify(loadUserPort).findByEmail(email);
        inOrder.verify(hasher).hash(rawPassword);
        inOrder.verify(saveUserPort).save(argThat(u ->
                u.email().equals(email)
                        && !u.emailVerified()
                        && u.roles().contains(Role.USER)
        ));
        inOrder.verify(tokens).generateVerificationToken(any(User.class));
        inOrder.verify(mailer).sendVerificationEmail(eq(email.getValue()),
                eq(apiBaseUrl + "/auth/verify?token=" + token));
        inOrder.verifyNoMoreInteractions();
    }
}

