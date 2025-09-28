package com.aredondocharro.ClothingStore.notificationsTEST.application;


import com.aredondocharro.ClothingStore.notification.application.SendEmailService;
import com.aredondocharro.ClothingStore.notification.domain.model.Email;
import com.aredondocharro.ClothingStore.notification.domain.model.EmailAddress;
import com.aredondocharro.ClothingStore.notification.domain.port.out.EmailSenderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SendEmailServiceTest {

    private EmailSenderPort emailSender;
    private SendEmailService service;

    @BeforeEach
    void setUp() {
        emailSender = Mockito.mock(EmailSenderPort.class);
        service = new SendEmailService(emailSender);
    }

    @Test
    void builds_email_and_delegates_to_port_with_explicit_from() {
        service.send("from@me.com", List.of("to@you.com"), "Hi", "Body", false);

        ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);
        verify(emailSender, times(1)).send(captor.capture());
        Email e = captor.getValue();

        assertThat(e.from()).extracting(EmailAddress::value).isEqualTo("from@me.com");
        assertThat(e.to()).extracting(EmailAddress::value).containsExactly("to@you.com");
        assertThat(e.subject()).isEqualTo("Hi");
        assertThat(e.body()).isEqualTo("Body");
        assertThat(e.html()).isFalse();
    }

    @Test
    void null_or_blank_from_is_allowed_and_becomes_null() {
        service.send(null, List.of("a@b.com"), "S", "B", true);
        service.send("   ", List.of("a@b.com"), "S", "B", true);

        ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);
        verify(emailSender, times(2)).send(captor.capture());
        assertThat(captor.getAllValues()).allSatisfy(e -> {
            assertThat(e.from()).isNull();
            assertThat(e.html()).isTrue();
        });
    }

    @Test
    void invalid_recipient_throws_before_port_call() {
        assertThrows(IllegalArgumentException.class,
                () -> service.send(null, List.of("bad-email"), "S", "B", false));
        verifyNoInteractions(emailSender);
    }


}