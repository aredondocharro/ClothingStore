// src/main/java/com/aredondocharro/ClothingStore/notification/infrastructure/out/template/ThymeleafTemplateRendererAdapter.java
package com.aredondocharro.ClothingStore.notification.infrastructure.out.template;

import com.aredondocharro.ClothingStore.notification.domain.port.out.TemplateRendererPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ThymeleafTemplateRendererAdapter implements TemplateRendererPort {

    @Qualifier("emailTemplateEngine")
    private final SpringTemplateEngine emailTemplateEngine;

    @Qualifier("emailMessageSource")
    private final MessageSource emailMessageSource;

    @Override
    public RenderedEmail render(String templateId, Map<String, Object> model, Locale locale) {
        Locale loc = (locale != null) ? locale : Locale.getDefault();

        Context ctx = new Context(loc);
        if (model != null && !model.isEmpty()) {
            ctx.setVariables(model);
        }

        // 1) HTML
        String html = emailTemplateEngine.process(templateId, ctx);

        // 2) Subject key por plantilla
        String subjectKey;
        switch (templateId) {
            case "verify-email":
            case "verify":
                subjectKey = "mail.verify.subject";
                break;
            case "password-reset":
                subjectKey = "mail.reset.subject";
                break;
            default:
                subjectKey = "mail.subject"; // si no tienes esta clave, fallará abajo
        }

        // 3) Subject i18n (sin fallback: si falta, lanza error claro)
        String subject;
        try {
            subject = emailMessageSource.getMessage(subjectKey, null, loc);
        } catch (NoSuchMessageException ex) {
            throw new IllegalStateException(
                    "Missing i18n key '" + subjectKey + "' for templateId='" + templateId +
                            "' and locale=" + loc + ". Revisa basenames del MailConfig y los bundles en src/main/resources/i18n/.",
                    ex
            );
        }

        log.debug("Rendered email templateId={} locale={} subjectKey={} modelKeys={}",
                templateId, loc, subjectKey, (model != null ? model.keySet() : List.of()));

        return new RenderedEmail(subject, html);
    }
}
