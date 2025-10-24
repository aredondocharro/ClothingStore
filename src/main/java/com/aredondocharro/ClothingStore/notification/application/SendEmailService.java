package com.aredondocharro.ClothingStore.notification.application;

import com.aredondocharro.ClothingStore.notification.domain.model.EmailMessage;
import com.aredondocharro.ClothingStore.notification.domain.model.EmailAddress;
import com.aredondocharro.ClothingStore.notification.domain.port.in.SendEmailUseCase;
import com.aredondocharro.ClothingStore.notification.domain.port.out.EmailSenderPort;
import com.aredondocharro.ClothingStore.notification.domain.port.out.TemplateRendererPort;
import com.aredondocharro.ClothingStore.notification.domain.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import java.util.*;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Slf4j
public class SendEmailService implements SendEmailUseCase {

    private final TemplateRendererPort renderer;
    private final EmailSenderPort emailSender;

    @Override
    public void send(String fromOrNull,
                     List<String> to,
                     String templateId,
                     Map<String, Object> model,
                     Locale locale) {

        // 1) Minimal rules
        if (templateId == null || templateId.isBlank()) throw new TemplateIdRequiredException();
        if (to == null) throw new RecipientsRequiredException();

        boolean fromProvided = fromOrNull != null && !fromOrNull.isBlank();
        int originalCount = to.size();
        Locale loc = (locale != null) ? locale : Locale.getDefault();
        log.debug("SendEmail invoked (templateId={}, locale={}, recipientsBefore={}, fromProvided={})",
                templateId, loc, originalCount, fromProvided);

        // 2) Normalize & deduplicate recipients (keep order)
        LinkedHashSet<String> cleaned = to.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (cleaned.isEmpty()) throw new RecipientsRequiredException();

        int afterCount = cleaned.size();
        int removed = Math.max(0, originalCount - afterCount);
        log.debug("Recipients normalized and de-duplicated (after={}, removed={})", afterCount, removed);

        EmailAddress from = fromProvided ? new EmailAddress(fromOrNull) : null;
        List<EmailAddress> recipients = cleaned.stream().map(EmailAddress::new).toList();

        Map<String,Object> safeModel = (model != null) ? model : Map.of();

        // Render
        log.debug("Rendering template (templateId={}, locale={})", templateId, loc);
        TemplateRendererPort.RenderedEmail rendered = renderer.render(templateId, safeModel, loc);
        log.info("Template rendered successfully (templateId={}, locale={})", templateId, loc);

        EmailMessage emailMessage = new EmailMessage(from, recipients, rendered.subject(), rendered.bodyHtml(), true);

        // 3) Send (map provider errors to domain)
        try {
            log.debug("Dispatching email (templateId={}, recipientsCount={}, html={})",
                    templateId, recipients.size(), true);
            emailSender.send(emailMessage);
            log.info("Email queued/sent successfully (templateId={}, recipientsCount={})",
                    templateId, recipients.size());
        } catch (Exception e) {
            log.error("Email provider failed (templateId={}, recipientsCount={}). Reason: {}",
                    templateId, recipients.size(), e.getMessage(), e);
            throw new EmailSendFailedException("email send failed", e);
        }
    }
}
