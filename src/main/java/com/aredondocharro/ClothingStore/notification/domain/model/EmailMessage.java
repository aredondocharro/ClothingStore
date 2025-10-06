package com.aredondocharro.ClothingStore.notification.domain.model;

import com.aredondocharro.ClothingStore.notification.domain.exception.RecipientsRequiredException;
import com.aredondocharro.ClothingStore.notification.domain.exception.SubjectRequiredException;
import com.aredondocharro.ClothingStore.notification.domain.exception.BodyRequiredException;
import com.aredondocharro.ClothingStore.notification.domain.exception.InvalidEmailAddressException;

import java.util.List;
import java.util.Objects;

public record EmailMessage(EmailAddress from, List<EmailAddress> to, String subject, String body, boolean html) {
    public EmailMessage {
        // to: obligatorio y sin elementos nulos
        if (to == null || to.isEmpty()) throw new RecipientsRequiredException();
        if (to.stream().anyMatch(Objects::isNull)) throw new InvalidEmailAddressException();

        // subject: obligatorio (limpiamos espacios laterales)
        subject = (subject == null) ? null : subject.strip();
        if (subject == null || subject.isBlank()) throw new SubjectRequiredException();

        // body: obligatorio
        if (body == null || body.isBlank()) throw new BodyRequiredException();

        // inmutabilidad defensiva
        to = List.copyOf(to);
    }
}
