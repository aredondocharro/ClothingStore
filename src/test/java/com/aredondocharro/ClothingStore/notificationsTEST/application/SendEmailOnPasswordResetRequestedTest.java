package com.aredondocharro.ClothingStore.notificationsTEST.application;

import com.aredondocharro.ClothingStore.identity.contracts.event.PasswordResetEmailRequested;
import com.aredondocharro.ClothingStore.notification.application.SendEmailOnPasswordResetRequested;
import com.aredondocharro.ClothingStore.notification.domain.port.in.SendEmailUseCase;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.mockito.Mockito.*;

class SendEmailOnPasswordResetRequestedTest {

    @Test
    void delegatesToSendEmailUseCase() {
        SendEmailUseCase useCase = mock(SendEmailUseCase.class);
        SendEmailOnPasswordResetRequested handler = new SendEmailOnPasswordResetRequested(useCase);

        var evt = new PasswordResetEmailRequested("u@example.com", "https://x/reset?t=abc", Instant.now());
        handler.on(evt);

        verify(useCase).send(
                isNull(),
                eq(List.of("u@example.com")),
                eq("password-reset"),
                eq(Map.of("resetUrl", "https://x/reset?t=abc")),
                any(Locale.class)
        );
    }
}
