package com.aredondocharro.ClothingStore.notification.application;

import com.aredondocharro.ClothingStore.notification.domain.model.Email;
import com.aredondocharro.ClothingStore.notification.domain.model.EmailAddress;
import com.aredondocharro.ClothingStore.notification.domain.port.in.SendEmailUseCase;
import com.aredondocharro.ClothingStore.notification.domain.port.out.EmailSenderPort;
import com.aredondocharro.ClothingStore.notification.domain.port.out.TemplateRendererPort;
import com.aredondocharro.ClothingStore.notification.domain.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SendEmailService implements SendEmailUseCase {

    private final TemplateRendererPort renderer;
    private final EmailSenderPort emailSender;

    @Override
    public void send(String fromOrNull,
                     List<String> to,
                     String templateId,
                     Map<String, Object> model,
                     Locale locale) {

        // 1) Reglas mínimas
        if (templateId == null || templateId.isBlank()) throw new TemplateIdRequiredException();
        if (to == null) throw new RecipientsRequiredException();

        // 2) Normaliza y deduplica destinatarios (conservando orden)
        LinkedHashSet<String> cleaned = to.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (cleaned.isEmpty()) throw new RecipientsRequiredException();

        EmailAddress from = (fromOrNull == null || fromOrNull.isBlank()) ? null : new EmailAddress(fromOrNull);
        List<EmailAddress> recipients = cleaned.stream().map(EmailAddress::new).toList();

        Locale loc = (locale != null) ? locale : Locale.getDefault();
        Map<String,Object> safeModel = (model != null) ? model : Map.of();

        // Renderizado (si el puerto lanza TemplateNotFound/RenderException, dejar pasar)
        TemplateRendererPort.RenderedEmail rendered = renderer.render(templateId, safeModel, loc);

        Email email = new Email(from, recipients, rendered.subject(), rendered.bodyHtml(), true);

        // 3) Mapea errores del provider a excepción de dominio
        try {
            emailSender.send(email);
        } catch (Exception e) {
            // Evita filtrar PII en el mensaje
            throw new EmailSendFailedException("email send failed", e);
        }
    }
}

