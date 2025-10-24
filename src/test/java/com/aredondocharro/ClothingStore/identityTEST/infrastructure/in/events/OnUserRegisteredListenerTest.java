package com.aredondocharro.ClothingStore.identityTEST.infrastructure.in.events;

import com.aredondocharro.ClothingStore.identity.application.PublishVerificationEmailOnUserRegisteredService;
import com.aredondocharro.ClothingStore.identity.contracts.event.UserRegistered;
import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.infrastructure.in.events.OnUserRegisteredListener;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.*;

class OnUserRegisteredListenerTest {

    @Test
    void listenerDelegatesToHandler() {
        PublishVerificationEmailOnUserRegisteredService handler = mock(PublishVerificationEmailOnUserRegisteredService.class);
        OnUserRegisteredListener listener = new OnUserRegisteredListener(handler);

        var evt = new UserRegistered(UserId.newId().value(), IdentityEmail.of("u@example.com").getValue(), Instant.now());
        listener.on(evt);

        verify(handler).on(evt);
    }
}
