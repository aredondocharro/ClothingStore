package com.aredondocharro.ClothingStore.notificationsTEST.infrastructure.out.smtp;


import com.aredondocharro.ClothingStore.notification.infrastructure.out.smtp.SmtpEmailSenderAdapter;
import com.aredondocharro.ClothingStore.notification.config.AppMailProperties;
import com.aredondocharro.ClothingStore.notification.domain.model.Email;
import com.aredondocharro.ClothingStore.notification.domain.model.EmailAddress;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SmtpEmailSenderAdapterTest {

    JavaMailSender mailSender;
    AppMailProperties props;
    SmtpEmailSenderAdapter adapter;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        props = new AppMailProperties();
        props.setDefaultFrom("default@store.com");
        props.setDefaultReplyTo("reply@store.com");
        adapter = new SmtpEmailSenderAdapter(mailSender, props);
    }

    @Test
    void send_plain_uses_simple_mail_message_and_defaults() {
        // Arrange: mock creation to capture SimpleMailMessage
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        doNothing().when(mailSender).send(captor.capture());

        Email email = new Email(null, List.of(new EmailAddress("to@x.com")), "S", "B", false);

        // Act
        adapter.send(email);

        // Assert
        SimpleMailMessage msg = captor.getValue();
        assertThat(msg.getTo()).containsExactly("to@x.com");
        assertThat(msg.getSubject()).isEqualTo("S");
        assertThat(msg.getText()).isEqualTo("B");
        assertThat(msg.getFrom()).isEqualTo("default@store.com");
        assertThat(msg.getReplyTo()).isEqualTo("reply@store.com");
    }

    @Test
    void send_html_builds_mime_message() throws Exception {
        // Arrange: real MimeMessage instance, captured via doAnswer
        MimeMessage mime = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mime);

        Email email = new Email(new EmailAddress("from@me.com"), List.of(new EmailAddress("to@x.com")), "S", "<b>B</b>", true);

        // Act
        adapter.send(email);

        // Assert: fields are set on MimeMessage
        assertThat(mime.getSubject()).isEqualTo("S");
        assertThat(mime.getFrom()[0].toString()).contains("from@me.com");
        assertThat(mime.getAllRecipients()[0].toString()).contains("to@x.com");
        // the HTML body is set via MimeMessageHelper; we can't easily assert the content without parsing multipart,
        // but reaching this point without exception is a good signal for unit test level.
    }
}