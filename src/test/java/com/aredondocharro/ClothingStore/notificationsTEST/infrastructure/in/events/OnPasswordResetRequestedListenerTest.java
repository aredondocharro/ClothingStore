package com.aredondocharro.ClothingStore.notificationsTEST.infrastructure.in.events;

import com.aredondocharro.ClothingStore.identity.contracts.event.PasswordResetEmailRequested;
import com.aredondocharro.ClothingStore.notification.application.SendEmailOnPasswordResetRequested;
import com.aredondocharro.ClothingStore.notification.infrastructure.in.events.OnPasswordResetRequestedListener;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.*;

class OnPasswordResetRequestedListenerTest {

    @Test
    void listenerDelegatesToHandler() {
        SendEmailOnPasswordResetRequested handler = mock(SendEmailOnPasswordResetRequested.class);
        OnPasswordResetRequestedListener listener = new OnPasswordResetRequestedListener(handler);

        var evt = new PasswordResetEmailRequested("u@example.com", "https://x/reset?t=abc", Instant.now());
        listener.on(evt);

        verify(handler).on(evt);
    }
}
