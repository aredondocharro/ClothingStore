package com.aredondocharro.ClothingStore.notification.domain.model;

import java.util.List;

public record Email(EmailAddress from, List<EmailAddress> to, String subject, String body, boolean html) {
    public Email {
        if (to == null || to.isEmpty()) throw new IllegalArgumentException("'to' cannot be empty");
        if (subject == null || subject.isBlank()) throw new IllegalArgumentException("'subject' is required");
        if (body == null || body.isBlank()) throw new IllegalArgumentException("'body' is required");
        to = List.copyOf(to);
    }
}
