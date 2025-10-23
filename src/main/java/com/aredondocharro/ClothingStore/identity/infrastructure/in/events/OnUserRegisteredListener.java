package com.aredondocharro.ClothingStore.identity.infrastructure.in.events;

import com.aredondocharro.ClothingStore.identity.application.PublishVerificationEmailOnUserRegisteredService;
import com.aredondocharro.ClothingStore.identity.contracts.event.UserRegistered;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OnUserRegisteredListener {

    private final PublishVerificationEmailOnUserRegisteredService handler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(UserRegistered e) {
        handler.on(e);
    }
}
