package com.aredondocharro.ClothingStore.identity.infrastructure.out.mail;

import com.aredondocharro.ClothingStore.identity.domain.port.out.MailerPort;
import com.aredondocharro.ClothingStore.notification.domain.port.in.SendEmailUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.aredondocharro.ClothingStore.shared.log.LogSanitizer.maskEmail;

@Slf4j
@RequiredArgsConstructor
public class MailerAdapter implements MailerPort {

    private final SendEmailUseCase sendEmail;
    private final String defaultFromAddress;
    private final String verifyTemplateId;
    private final String resetTemplateId;

    @Override
    public void sendVerificationEmail(String to, String verificationUrl) {
        String from = defaultFromAddress;
        List<String> recipients = List.of(to);
        String templateId = verifyTemplateId; // <-- use injected id
        Map<String, Object> model = Map.of(
                "verificationUrl", verificationUrl,
                "email", to
        );
        Locale locale = LocaleContextHolder.getLocale();

        log.info("Enqueue verification email templateId={} to={}", templateId, maskEmail(to));
        sendEmail.send(from, recipients, templateId, model, locale);
    }

    @Override
    public void sendPasswordResetLink(String to, String resetLink) {
        String from = defaultFromAddress;
        List<String> recipients = List.of(to);
        String templateId = resetTemplateId; // <-- use injected id
        Map<String, Object> model = Map.of(
                "resetUrl", resetLink,
                "email", to
        );
        Locale locale = LocaleContextHolder.getLocale();

        log.info("Enqueue password reset email templateId={} to={}", templateId, maskEmail(to));
        sendEmail.send(from, recipients, templateId, model, locale);
    }
}

