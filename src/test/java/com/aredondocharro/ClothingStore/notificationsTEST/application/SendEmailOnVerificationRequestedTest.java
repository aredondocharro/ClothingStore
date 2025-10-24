package com.aredondocharro.ClothingStore.notificationsTEST.application;

import com.aredondocharro.ClothingStore.identity.contracts.event.VerificationEmailRequested;
import com.aredondocharro.ClothingStore.notification.application.SendEmailOnVerificationRequested;
import com.aredondocharro.ClothingStore.notification.domain.port.in.SendEmailUseCase;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.mockito.Mockito.*;

class SendEmailOnVerificationRequestedTest {

    @Test
    void delegatesToSendEmailUseCase() {
        SendEmailUseCase useCase = mock(SendEmailUseCase.class);
        SendEmailOnVerificationRequested handler = new SendEmailOnVerificationRequested(useCase);

        var evt = new VerificationEmailRequested(("u@example.com"), "https://x/verify?t=abc", Instant.now());
        handler.on(evt);

        verify(useCase).send(
                isNull(),
                eq(List.of("u@example.com")),
                eq("verify-email"),
                eq(Map.of("verificationUrl", "https://x/verify?t=abc")),
                any(Locale.class)
        );
    }
}
