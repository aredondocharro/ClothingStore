package com.aredondocharro.ClothingStore.notification.infrastructure.in.events;

import com.aredondocharro.ClothingStore.identity.contracts.event.PasswordResetEmailRequested;
import com.aredondocharro.ClothingStore.notification.application.SendEmailOnPasswordResetRequested;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OnPasswordResetRequestedListener {

    private final SendEmailOnPasswordResetRequested handler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(PasswordResetEmailRequested e) {
        handler.on(e);
    }
}
