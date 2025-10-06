package com.aredondocharro.ClothingStore.notification.infrastructure.out.smtp;

import com.aredondocharro.ClothingStore.notification.domain.model.EmailMessage;
import com.aredondocharro.ClothingStore.notification.infrastructure.config.AppMailProperties;
import com.aredondocharro.ClothingStore.notification.domain.model.EmailAddress;
import com.aredondocharro.ClothingStore.notification.domain.port.out.EmailSenderPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SmtpEmailSenderAdapter implements EmailSenderPort {

    private final JavaMailSender mailSender;
    private final AppMailProperties props;

    @Override
    public void send(EmailMessage emailMessage) {
        if (emailMessage.html()) sendHtml(emailMessage);
        else sendText(emailMessage);
    }

    private void sendText(EmailMessage emailMessage) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(emailMessage.to().stream().map(EmailAddress::value).toArray(String[]::new));
        msg.setSubject(emailMessage.subject());
        msg.setText(emailMessage.body());

        String from = effectiveFrom(emailMessage);
        if (from != null) msg.setFrom(from);
        if (props.getDefaultReplyTo() != null) msg.setReplyTo(props.getDefaultReplyTo());

        mailSender.send(msg);
    }

    private void sendHtml(EmailMessage emailMessage) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(mime, "UTF-8");
            h.setTo(emailMessage.to().stream().map(EmailAddress::value).toArray(String[]::new));
            h.setSubject(emailMessage.subject());
            h.setText(emailMessage.body(), true);

            String from = effectiveFrom(emailMessage);
            if (from != null) h.setFrom(from);
            if (props.getDefaultReplyTo() != null) h.setReplyTo(props.getDefaultReplyTo());

            mailSender.send(mime);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    @Nullable
    private String effectiveFrom(EmailMessage emailMessage) {
        if (emailMessage.from() != null) return emailMessage.from().value();
        return props.getDefaultFrom(); // si también es null, el servidor podría asignar uno (no ideal)
    }
}
