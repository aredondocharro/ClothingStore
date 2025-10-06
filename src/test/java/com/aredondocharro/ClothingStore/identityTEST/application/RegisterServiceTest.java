package com.aredondocharro.ClothingStore.identityTEST.application;

import com.aredondocharro.ClothingStore.identity.application.RegisterUserService;
import com.aredondocharro.ClothingStore.identity.domain.exception.EmailAlreadyExistException;
import com.aredondocharro.ClothingStore.identity.domain.exception.PasswordMismatchException;
import com.aredondocharro.ClothingStore.identity.domain.exception.PasswordRequiredException;
import com.aredondocharro.ClothingStore.identity.domain.model.Email;
import com.aredondocharro.ClothingStore.identity.domain.model.Role;
import com.aredondocharro.ClothingStore.identity.domain.model.User;
import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.out.LoadUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.MailerPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordHasherPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordPolicyPort;   // NUEVO
import com.aredondocharro.ClothingStore.identity.domain.port.out.SaveUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenGeneratorPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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
    @Mock PasswordPolicyPort passwordPolicy; // NUEVO

    RegisterUserService service;

    final String apiBaseUrl = "https://api.example.com";

    @BeforeEach
    void setUp() {
        service = new RegisterUserService(
                loadUserPort, saveUserPort, hasher, tokens, mailer, apiBaseUrl, passwordPolicy // NUEVO
        );
    }

    @Test
    void register_nullOrBlankPassword_throwsPasswordRequired_andDoesNotHitPorts() {
        Email email = Email.of("user@example.com");

        assertAll(
                () -> assertThrows(PasswordRequiredException.class, () -> service.register(email, null, "x")),
                () -> assertThrows(PasswordRequiredException.class, () -> service.register(email, "", "x")),
                () -> assertThrows(PasswordRequiredException.class, () -> service.register(email, "   ", "x"))
        );

        // No se debe llamar a NINGÚN puerto si la password es nula/blanca
        verifyNoInteractions(loadUserPort, hasher, saveUserPort, tokens, mailer, passwordPolicy);
    }

    @Test
    void register_passwordsDoNotMatch_throwsPasswordMismatch_andDoesNotHashOrSave() {
        Email email = Email.of("user@example.com");
        String raw = "Secret123!";
        String confirm = "Other123!";

        assertThrows(PasswordMismatchException.class, () -> service.register(email, raw, confirm));

        // Se valida la contraseña y se corta antes de tocar persistencia/tokens/mails
        verify(passwordPolicy).validate(raw);
        verifyNoInteractions(loadUserPort, hasher, saveUserPort, tokens, mailer);
    }

    @Test
    void register_existingEmail_throwsEmailAlreadyExistException_andDoesNotHashOrSave() {
        Email email = Email.of("user@example.com");
        String raw = "Secret123!";

        // simulamos que el usuario ya existe
        when(loadUserPort.findByEmail(email)).thenReturn(Optional.of(mock(User.class)));

        assertThrows(EmailAlreadyExistException.class, () -> service.register(email, raw, raw));

        // Orden: primero política, luego unicidad
        InOrder inOrder = inOrder(passwordPolicy, loadUserPort);
        inOrder.verify(passwordPolicy).validate(raw);
        inOrder.verify(loadUserPort).findByEmail(email);
        inOrder.verifyNoMoreInteractions();

        // No debe hashear/guardar/generar/enviar
        verifyNoInteractions(hasher, saveUserPort, tokens, mailer);
    }

    @Test
    void register_success_hashes_saves_generatesToken_and_sendsVerificationEmail() {
        Email email = Email.of("user@example.com");
        String rawPassword = "Secret123!";

        when(loadUserPort.findByEmail(email)).thenReturn(Optional.empty());

        // Hash bcrypt válido (60 chars)
        String bcrypt = "$2b$10$7EqJtq98hPqEX7fNZaFWoO5f.Pg3rQAYyu3iJ/T9Y2aXx1Z9E6iGa";
        when(hasher.hash(rawPassword)).thenReturn(bcrypt);

        // save devuelve el mismo User que recibe
        when(saveUserPort.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0, User.class));

        String token = "verification-token-123";
        when(tokens.generateVerificationToken(any(User.class))).thenReturn(token);

        AuthResult result = service.register(email, rawPassword, rawPassword);

        // resultado (tu servicio devuelve null/null por diseño)
        assertNotNull(result);
        assertNull(result.accessToken());
        assertNull(result.refreshToken());

        // orden y llamadas (ahora empieza por passwordPolicy.validate)
        InOrder inOrder = inOrder(passwordPolicy, loadUserPort, hasher, saveUserPort, tokens, mailer);
        inOrder.verify(passwordPolicy).validate(rawPassword); // primero, política
        inOrder.verify(loadUserPort).findByEmail(email);
        inOrder.verify(hasher).hash(rawPassword);
        inOrder.verify(saveUserPort).save(argThat(u ->
                u.email().equals(email)
                        && !u.emailVerified()
                        && u.roles().contains(Role.USER)
        ));
        inOrder.verify(tokens).generateVerificationToken(any(User.class));

        // capturamos argumentos del mailer para no depender del link exacto
        ArgumentCaptor<String> toCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> linkCap = ArgumentCaptor.forClass(String.class);
        inOrder.verify(mailer).sendVerificationEmail(toCap.capture(), linkCap.capture());
        inOrder.verifyNoMoreInteractions();

        assertEquals(email.getValue(), toCap.getValue());

        String link = linkCap.getValue();
        // Debe empezar por el base URL que recibe el servicio
        assertTrue(link.startsWith(apiBaseUrl), "verification link must start with the configured base URL");
        // Debe incluir el token (en claro o URL-encoded)
        assertTrue(
                link.contains("token=" + token) ||
                        link.contains("token=" + java.net.URLEncoder.encode(token, java.nio.charset.StandardCharsets.UTF_8)),
                "verification link must include the token query param"
        );
    }
}
