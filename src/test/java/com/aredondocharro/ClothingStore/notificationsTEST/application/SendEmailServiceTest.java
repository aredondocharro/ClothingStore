package com.aredondocharro.ClothingStore.notificationsTEST.application;

import com.aredondocharro.ClothingStore.notification.application.SendEmailService;
import com.aredondocharro.ClothingStore.notification.domain.model.Email;
import com.aredondocharro.ClothingStore.notification.domain.model.EmailAddress;
import com.aredondocharro.ClothingStore.notification.domain.port.out.EmailSenderPort;
import com.aredondocharro.ClothingStore.notification.domain.port.out.TemplateRendererPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SendEmailServiceTest {

    private TemplateRendererPort renderer;
    private EmailSenderPort emailSender;
    private SendEmailService service;

    @BeforeEach
    void setUp() {
        renderer = mock(TemplateRendererPort.class);
        emailSender = mock(EmailSenderPort.class);
        service = new SendEmailService(renderer, emailSender);
    }

    @Test
    void builds_email_and_delegates_to_port_with_explicit_from() {
        // given
        var templateId = "verify-email";
        Map<String, Object> model = Map.of("verificationUrl", "http://x", "email", "to@you.com");
        var rendered = new TemplateRendererPort.RenderedEmail("Hi", "<p>Body</p>");
        when(renderer.render(eq(templateId), anyMap(), any(Locale.class)))
                .thenReturn(rendered);

        // when
        service.send("from@me.com", List.of("to@you.com"), templateId, model, Locale.ENGLISH);

        // then
        ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);
        verify(emailSender, times(1)).send(captor.capture());
        Email e = captor.getValue();

        assertThat(e.from()).extracting(EmailAddress::value).isEqualTo("from@me.com");
        assertThat(e.to()).extracting(EmailAddress::value).containsExactly("to@you.com");
        assertThat(e.subject()).isEqualTo("Hi");            // viene del renderer
        assertThat(e.body()).isEqualTo("<p>Body</p>");      // viene del renderer
        assertThat(e.html()).isTrue();                    // el servicio envía HTML
    }

    @Test
    void null_or_blank_from_is_allowed_and_becomes_null() {
        var templateId = "tpl";
        var rendered = new TemplateRendererPort.RenderedEmail("S", "<b>B</b>");
        when(renderer.render(eq(templateId), anyMap(), any(Locale.class)))
                .thenReturn(rendered);

        service.send(null, List.of("a@b.com"), templateId, Map.of(), Locale.getDefault());
        service.send("   ", List.of("a@b.com"), templateId, Map.of(), Locale.getDefault());

        ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);
        verify(emailSender, times(2)).send(captor.capture());
        assertThat(captor.getAllValues()).allSatisfy(e -> {
            assertThat(e.from()).isNull();
            assertThat(e.html()).isTrue();
        });
    }

    @Test
    void invalid_recipient_throws_before_port_call() {
        var templateId = "tpl";
        // no debería llamar al renderer si el destinatario es inválido
        assertThrows(IllegalArgumentException.class, () ->
                service.send(null, List.of("bad-email"), templateId, Map.of(), Locale.getDefault())
        );
        verifyNoInteractions(renderer);
        verifyNoInteractions(emailSender);
    }
}
