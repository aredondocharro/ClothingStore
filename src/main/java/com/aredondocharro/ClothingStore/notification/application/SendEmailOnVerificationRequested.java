package com.aredondocharro.ClothingStore.notification.application;

import com.aredondocharro.ClothingStore.identity.contracts.event.VerificationEmailRequested;
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
public class SendEmailOnVerificationRequested {

    private final SendEmailUseCase sendEmail;

@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(VerificationEmailRequested e) {
        sendEmail.send(
                null,
                List.of(e.email().getValue()),
                "verify-email",
                Map.of("verificationUrl", e.url()),
                Locale.getDefault()
        );
        log.info("Verification email queued to: {}", e.email());
    }
}