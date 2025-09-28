package com.aredondocharro.ClothingStore.notification.domain.port.in;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface SendEmailUseCase {
    void send(String fromOrNull, List<String> to, String templateId, Map<String,Object> model, Locale locale);
}

