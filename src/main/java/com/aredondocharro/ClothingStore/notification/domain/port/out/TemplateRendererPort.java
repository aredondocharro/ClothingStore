package com.aredondocharro.ClothingStore.notification.domain.port.out;

import java.util.Locale;
import java.util.Map;

public interface TemplateRendererPort {
    record RenderedEmail(String subject, String bodyHtml) {}
    RenderedEmail render(String templateId, Map<String,Object> model, Locale locale);
}
