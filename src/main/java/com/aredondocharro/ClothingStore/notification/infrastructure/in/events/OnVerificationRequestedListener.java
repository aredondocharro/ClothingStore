package com.aredondocharro.ClothingStore.notification.infrastructure.in.events;

import com.aredondocharro.ClothingStore.identity.contracts.event.VerificationEmailRequested;
import com.aredondocharro.ClothingStore.notification.application.SendEmailOnVerificationRequested;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OnVerificationRequestedListener {

    private final SendEmailOnVerificationRequested handler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(VerificationEmailRequested e) {
        handler.on(e);
    }
}
