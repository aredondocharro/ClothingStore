package com.aredondocharro.ClothingStore.notification.application;

import com.aredondocharro.ClothingStore.identity.contracts.event.VerificationEmailRequested;
import com.aredondocharro.ClothingStore.notification.domain.port.in.SendEmailUseCase;
import com.aredondocharro.ClothingStore.shared.log.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class SendEmailOnVerificationRequested {

    private static final String TEMPLATE_ID = "verify-email";

    private final SendEmailUseCase sendEmail;

    public void on(VerificationEmailRequested e) {
        String masked = LogSanitizer.maskEmail(e.email());
        log.debug("Handling VerificationEmailRequested event (email={}, templateId={})", masked, TEMPLATE_ID);
        try {
            sendEmail.send(
                    null,
                    List.of(e.email()),
                    TEMPLATE_ID,
                    Map.of("verificationUrl", e.url()),
                    Locale.getDefault()
            );
            log.info("Verification email queued successfully (email={}, templateId={})", masked, TEMPLATE_ID);
        } catch (RuntimeException ex) {
            log.error("Failed to queue verification email (email={}, templateId={}). Reason: {}",
                    masked, TEMPLATE_ID, ex.getMessage(), ex);
            throw ex;
        }
    }
}
