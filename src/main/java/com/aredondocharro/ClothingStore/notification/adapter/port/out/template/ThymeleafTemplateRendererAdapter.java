// src/main/java/com/aredondocharro/ClothingStore/notification/adapter/port/out/template/ThymeleafTemplateRendererAdapter.java
package com.aredondocharro.ClothingStore.notification.adapter.port.out.template;

import com.aredondocharro.ClothingStore.notification.domain.port.out.TemplateRendererPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ThymeleafTemplateRendererAdapter implements TemplateRendererPort {

    private final @Qualifier("emailTemplateEngine") SpringTemplateEngine templateEngine;
    private final @Qualifier("emailMessageSource") MessageSource emailMessageSource;

    @Override
    public RenderedEmail render(String templateId, Map<String, Object> model, Locale locale) {
        Locale loc = (locale != null) ? locale : Locale.getDefault();

        // Asunto desde i18n: email.<templateId>.subject  -> email.verify-email.subject
        String subjectKey = "email." + templateId + ".subject";
        String subject = emailMessageSource.getMessage(subjectKey, argsFrom(model), "No subject", loc);

        // Cuerpo HTML desde plantilla (el resolver ya añade prefix/suffix)
        Context ctx = new Context(loc);
        if (model != null) ctx.setVariables(model);
        String html = templateEngine.process(templateId, ctx);

        log.debug("Rendered templateId={} locale={} subject='{}'", templateId, loc, subject);
        return new RenderedEmail(subject, html);
    }

    private Object[] argsFrom(Map<String, Object> model) {
        if (model == null) return new Object[0];
        // Prioriza orderId para asuntos como "Your order #{0}", si existe; si no, email
        Object orderId = model.get("orderId");
        if (orderId != null) return new Object[]{orderId};
        Object email = model.get("email");
        return (email != null) ? new Object[]{email} : new Object[0];
    }
}
