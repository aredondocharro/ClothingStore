package com.aredondocharro.ClothingStore.notification.adapter.port.out.template;

import com.aredondocharro.ClothingStore.notification.config.AppMailProperties;
import com.aredondocharro.ClothingStore.notification.domain.port.out.TemplateRendererPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ThymeleafTemplateRendererAdapter implements TemplateRendererPort {

    private final SpringTemplateEngine templateEngine;
    private final MessageSource emailMessageSource;
    private final AppMailProperties props; // (opcional) para brand/from, etc.

    @Override
    public RenderedEmail render(String templateId, Map<String, Object> model, Locale locale) {
        // 1) Subject desde i18n: email.<templateId>.subject
        var subjectKey = "email." + templateId + ".subject";
        var subject = emailMessageSource.getMessage(subjectKey, argsFrom(model), "No subject", locale);

        // 2) Cuerpo desde plantilla
        var ctx = new Context(locale);
        if (model != null) ctx.setVariables(model);

        // Pasamos SOLO el templateId; el resolver añade prefix/suffix
        var html = templateEngine.process(templateId, ctx);

        return new RenderedEmail(subject, html);
    }

    private Object[] argsFrom(Map<String, Object> model) {
        if (model == null) return new Object[0];
        // Si quieres usarlo en el subject como {0}
        // p.ej.: email.verify-email.subject=Verify your email for {0}
        var email = model.get("email");
        return email != null ? new Object[]{ email } : new Object[0];
    }
}
