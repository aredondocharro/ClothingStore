package com.aredondocharro.ClothingStore.identityTEST.application;

import com.aredondocharro.ClothingStore.identity.application.ResendVerificationEmailService;
import com.aredondocharro.ClothingStore.identity.contracts.event.VerificationEmailRequested;
import com.aredondocharro.ClothingStore.identity.domain.model.*;
import com.aredondocharro.ClothingStore.identity.domain.port.out.LoadUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.VerificationTokenRotationPort;
import com.aredondocharro.ClothingStore.shared.domain.event.EventBusPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResendVerificationEmailServiceTest {

    @Mock LoadUserPort loadUserPort;
    @Mock VerificationTokenRotationPort tokenRotationPort;
    @Mock EventBusPort eventBus;

    private Clock clock;
    private ResendVerificationEmailService service;
    private Instant fixedNow;

    private static final String BCRYPT =
            "$2b$10$7EqJtq98hPqEX7fNZaFWoO5f.Pg3rQAYyu3iJ/T9Y2aXx1Z9E6iGa";

    @BeforeEach
    void setUp() {
        fixedNow = Instant.parse("2025-01-01T00:00:00Z");
        clock = Clock.fixed(fixedNow, ZoneOffset.UTC);

        // mismo orden que en la clase (RequiredArgsConstructor):
        // loadUserPort, tokenRotationPort, eventBus, clock, verifyBaseUrl
        service = new ResendVerificationEmailService(
                loadUserPort,
                tokenRotationPort,
                eventBus,
                clock,
                "https://example.com/auth/verify"
        );
    }

    @Test
    void resend_nonExistingEmail_doesNothing() {
        IdentityEmail email = IdentityEmail.of("no-user@example.com");

        when(loadUserPort.findByEmail(email)).thenReturn(Optional.empty());

        service.resend(email);

        verify(loadUserPort).findByEmail(email);
        verifyNoInteractions(tokenRotationPort, eventBus);
    }

    @Test
    void resend_alreadyVerifiedUser_doesNotRotateNorPublish() {
        IdentityEmail email = IdentityEmail.of("user@example.com");
        User user = new User(
                UserId.newId(),
                email,
                PasswordHash.ofHashed(BCRYPT),
                true, // ya verificado
                Set.of(Role.USER),
                fixedNow.minusSeconds(3600)
        );

        when(loadUserPort.findByEmail(email)).thenReturn(Optional.of(user));

        service.resend(email);

        verify(loadUserPort).findByEmail(email);
        verifyNoInteractions(tokenRotationPort, eventBus);
    }

    @Test
    void resend_notVerifiedUser_rotatesToken_andPublishesEvent() {
        IdentityEmail email = IdentityEmail.of("user@example.com");
        User user = new User(
                UserId.newId(),
                email,
                PasswordHash.ofHashed(BCRYPT),
                false, // NO verificado
                Set.of(Role.USER),
                fixedNow.minusSeconds(3600)
        );

        when(loadUserPort.findByEmail(email)).thenReturn(Optional.of(user));
        var rotated = new VerificationTokenRotationPort.RotatedVerificationToken(
                "token-123",
                fixedNow.plusSeconds(3600)
        );
        when(tokenRotationPort.rotateForUser(user)).thenReturn(rotated);

        service.resend(email);

        verify(loadUserPort).findByEmail(email);
        verify(tokenRotationPort).rotateForUser(user);

        ArgumentCaptor<VerificationEmailRequested> cap =
                ArgumentCaptor.forClass(VerificationEmailRequested.class);
        verify(eventBus).publish(cap.capture());

        VerificationEmailRequested evt = cap.getValue();
        assertEquals("user@example.com", evt.email());
        assertEquals("https://example.com/auth/verify?token=token-123", evt.url());
        assertEquals(fixedNow, evt.occurredAt());
    }
}
