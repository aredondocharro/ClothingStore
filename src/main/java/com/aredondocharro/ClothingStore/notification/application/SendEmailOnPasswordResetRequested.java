// src/main/java/com/aredondocharro/ClothingStore/notification/application/SendEmailOnPasswordResetRequested.java
package com.aredondocharro.ClothingStore.notification.application;

import com.aredondocharro.ClothingStore.identity.contracts.event.PasswordResetEmailRequested;
import com.aredondocharro.ClothingStore.notification.domain.port.in.SendEmailUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendEmailOnPasswordResetRequested {

    private final SendEmailUseCase sendEmail;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(PasswordResetEmailRequested e) {
        sendEmail.send(
                null,
                List.of(e.email()),
                "password-reset",
                Map.of("resetUrl", e.url()),
                Locale.getDefault()
        );
        log.info("Password reset email queued to {}", e.email());
    }
}
