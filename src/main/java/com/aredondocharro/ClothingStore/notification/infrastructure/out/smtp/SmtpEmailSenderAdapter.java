package com.aredondocharro.ClothingStore.notification.infrastructure.out.smtp;

import com.aredondocharro.ClothingStore.notification.domain.model.EmailAddress;
import com.aredondocharro.ClothingStore.notification.domain.model.EmailMessage;
import com.aredondocharro.ClothingStore.notification.infrastructure.config.AppMailProperties;
import com.aredondocharro.ClothingStore.notification.domain.port.out.EmailSenderPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.Nullable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.mail", name = "mode", havingValue = "direct", matchIfMissing = true)
@RequiredArgsConstructor
public class SmtpEmailSenderAdapter implements EmailSenderPort {

    private final JavaMailSender mailSender;
    private final AppMailProperties props;

    @Override
    public void send(EmailMessage emailMessage) {
        String[] to = emailMessage.to().stream().map(EmailAddress::value).toArray(String[]::new);
        String maskedTo = Arrays.stream(to).map(this::maskEmail).reduce((a,b) -> a + "," + b).orElse("<none>");
        String from = effectiveFrom(emailMessage);
        String maskedFrom = from == null ? "<default/null>" : maskEmail(from);
        boolean html = emailMessage.html();

        log.debug("[smtp] preparing {} email: to={} | subject='{}' | from={}",
                html ? "HTML" : "TEXT", maskedTo, emailMessage.subject(), maskedFrom);

        try {
            if (!html) {
                SimpleMailMessage msg = new SimpleMailMessage();
                if (from != null) msg.setFrom(from);
                if (props.getDefaultReplyTo() != null) msg.setReplyTo(props.getDefaultReplyTo());
                msg.setTo(to);
                msg.setSubject(emailMessage.subject());
                msg.setText(emailMessage.body());
                mailSender.send(msg);
            } else {
                MimeMessage mime = mailSender.createMimeMessage();
                MimeMessageHelper h = new MimeMessageHelper(mime, "UTF-8");
                if (from != null) h.setFrom(from);
                h.setTo(to);
                h.setSubject(emailMessage.subject());
                h.setText(emailMessage.body(), true);
                if (props.getDefaultReplyTo() != null) h.setReplyTo(props.getDefaultReplyTo());
                mailSender.send(mime);
            }

            log.info("[smtp] sent {} email to {} (subject='{}')",
                    html ? "HTML" : "TEXT", maskedTo, emailMessage.subject());

        } catch (MessagingException e) {
            log.error("[smtp] failed to send email to {} (subject='{}'): {}",
                    maskedTo, emailMessage.subject(), e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Nullable
    private String effectiveFrom(EmailMessage emailMessage) {
        if (emailMessage.from() != null) return emailMessage.from().value();
        return props.getDefaultFrom();
    }

    // Enmascara PII: "roxtrife@gmail.com" -> "r***@gmail.com"
    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "<null>";
        int at = email.indexOf('@');
        if (at <= 0) return "***";
        char first = email.charAt(0);
        String domain = email.substring(at);
        return first + "***" + domain;
    }
}
