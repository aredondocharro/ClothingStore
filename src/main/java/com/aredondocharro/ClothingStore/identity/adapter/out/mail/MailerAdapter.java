package com.aredondocharro.ClothingStore.identity.adapter.out.mail;

import com.aredondocharro.ClothingStore.identity.domain.port.out.MailerPort;
import com.aredondocharro.ClothingStore.notification.domain.port.in.SendEmailUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class MailerAdapter implements MailerPort {
    private final SendEmailUseCase sendEmail;

    @Override
    public void sendVerificationEmail(String to, String verificationUrl) {
        String from = null; // usa el 'default' en tu email sender si aplica
        List<String> recipients = List.of(to);
        String templateId = "verify-email";
        Map<String, Object> model = Map.of(
                "verificationUrl", verificationUrl,
                "email", to
        );
        Locale locale = Locale.getDefault();

        sendEmail.send(from, recipients, templateId, model, locale);
        log.info("Verification email enqueued to={}", to);
    }
}
