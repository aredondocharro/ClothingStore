package com.aredondocharro.ClothingStore.notification.adapter.port.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public record SendEmailRequest(
        String from,
        @NotEmpty List<String> to,
        @NotBlank String templateId,
        Map<String,Object> model,
        Locale locale
) {}

