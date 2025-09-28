package com.aredondocharro.ClothingStore.notification.adapter.port.out.smtp;

import com.aredondocharro.ClothingStore.notification.config.AppMailProperties;
import com.aredondocharro.ClothingStore.notification.domain.model.Email;
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
    public void send(Email email) {
        if (email.html()) sendHtml(email);
        else sendText(email);
    }

    private void sendText(Email email) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email.to().stream().map(EmailAddress::value).toArray(String[]::new));
        msg.setSubject(email.subject());
        msg.setText(email.body());

        String from = effectiveFrom(email);
        if (from != null) msg.setFrom(from);
        if (props.getDefaultReplyTo() != null) msg.setReplyTo(props.getDefaultReplyTo());

        mailSender.send(msg);
    }

    private void sendHtml(Email email) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(mime, "UTF-8");
            h.setTo(email.to().stream().map(EmailAddress::value).toArray(String[]::new));
            h.setSubject(email.subject());
            h.setText(email.body(), true);

            String from = effectiveFrom(email);
            if (from != null) h.setFrom(from);
            if (props.getDefaultReplyTo() != null) h.setReplyTo(props.getDefaultReplyTo());

            mailSender.send(mime);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    @Nullable
    private String effectiveFrom(Email email) {
        if (email.from() != null) return email.from().value();
        return props.getDefaultFrom(); // si también es null, el servidor podría asignar uno (no ideal)
    }
}
