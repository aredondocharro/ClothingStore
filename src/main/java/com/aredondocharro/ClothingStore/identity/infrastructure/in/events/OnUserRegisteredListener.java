package com.aredondocharro.ClothingStore.identity.infrastructure.in.events;

import com.aredondocharro.ClothingStore.identity.application.PublishVerificationEmailOnUserRegisteredService;
import com.aredondocharro.ClothingStore.identity.contracts.event.UserRegistered;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OnUserRegisteredListener {

    private final PublishVerificationEmailOnUserRegisteredService handler;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void on(UserRegistered e) {
        // We keep logs generic to avoid leaking PII (like email) in logs
        String eventType = e != null ? e.getClass().getSimpleName() : "UserRegistered";
        log.debug("Received '{}' event (phase=AFTER_COMMIT); delegating to PublishVerificationEmailOnUserRegisteredService.", eventType);

        try {
            handler.on(e);
            log.info("'{}' event handled successfully: verification email publication triggered.", eventType);
        } catch (RuntimeException ex) {
            log.error("Failed to handle '{}' event. Reason: {}", eventType, ex.getMessage(), ex);
            throw ex;
        }
    }
}
