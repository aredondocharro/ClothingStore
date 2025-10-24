package com.aredondocharro.ClothingStore.notification.application;

import com.aredondocharro.ClothingStore.identity.contracts.event.PasswordResetEmailRequested;
import com.aredondocharro.ClothingStore.notification.domain.port.in.SendEmailUseCase;
import com.aredondocharro.ClothingStore.shared.log.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class SendEmailOnPasswordResetRequested {

    private static final String TEMPLATE_ID = "password-reset";

    private final SendEmailUseCase sendEmail;

    public void on(PasswordResetEmailRequested e) {
        // Avoid logging URL or raw email (PII)
        String masked = LogSanitizer.maskEmail(e.email());
        log.debug("Handling PasswordResetEmailRequested event (email={}, templateId={})", masked, TEMPLATE_ID);

        try {
            sendEmail.send(
                    null,
                    List.of(e.email()),
                    TEMPLATE_ID,
                    Map.of("resetUrl", e.url()),
                    Locale.getDefault()
            );
            log.info("Password reset email queued successfully (email={}, templateId={})", masked, TEMPLATE_ID);
        } catch (RuntimeException ex) {
            log.error("Failed to queue password reset email (email={}, templateId={}). Reason: {}",
                    masked, TEMPLATE_ID, ex.getMessage(), ex);
            throw ex; // bubble up so error handling can act
        }
    }
}
