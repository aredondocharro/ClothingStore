package com.aredondocharro.ClothingStore.notification.application;


import com.aredondocharro.ClothingStore.notification.domain.model.Email;
import com.aredondocharro.ClothingStore.notification.domain.model.EmailAddress;
import com.aredondocharro.ClothingStore.notification.domain.port.in.SendEmailUseCase;
import com.aredondocharro.ClothingStore.notification.domain.port.out.EmailSenderPort;
import com.aredondocharro.ClothingStore.notification.domain.port.out.TemplateRendererPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SendEmailService implements SendEmailUseCase {

    private final TemplateRendererPort renderer;
    private final EmailSenderPort emailSender;

    @Override
    public void send(String fromOrNull, List<String> to, String templateId, Map<String,Object> model, Locale locale) {
        var from = (fromOrNull == null || fromOrNull.isBlank()) ? null : new EmailAddress(fromOrNull);
        var recipients = to.stream().map(EmailAddress::new).toList();

        var rendered = renderer.render(templateId, model, locale != null ? locale : Locale.getDefault());

        var email = new Email(from, recipients, rendered.subject(), rendered.bodyHtml(), true);
        emailSender.send(email);
    }
}
