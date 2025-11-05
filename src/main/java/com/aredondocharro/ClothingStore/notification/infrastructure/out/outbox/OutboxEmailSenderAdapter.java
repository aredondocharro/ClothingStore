package com.aredondocharro.ClothingStore.notification.infrastructure.out.outbox;

import com.aredondocharro.ClothingStore.notification.domain.model.EmailAddress;
import com.aredondocharro.ClothingStore.notification.domain.model.EmailMessage;
import com.aredondocharro.ClothingStore.notification.domain.port.out.EmailSenderPort;
import com.aredondocharro.ClothingStore.notification.infrastructure.out.persistence.entity.EmailOutboxEntity;
import com.aredondocharro.ClothingStore.notification.infrastructure.out.persistence.repository.EmailOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.mail", name = "mode", havingValue = "outbox")
@RequiredArgsConstructor
public class OutboxEmailSenderAdapter implements EmailSenderPort {

    private final EmailOutboxRepository repo;

    /**
     * REQUIRES_NEW para asegurar que el encolado se persiste incluso si el caso de uso exterior
     * (o el listener) acaba haciendo rollback por otra razón.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void send(EmailMessage email) {
        final String toCsv = email.to().stream()
                .map(EmailAddress::value)
                .collect(Collectors.joining(","));

        // Usa el builder (el constructor es protected)
        EmailOutboxEntity entity = EmailOutboxEntity.builder()
                .toAddresses(toCsv)
                .subject(email.subject())
                .body(email.body())
                .html(email.html())
                .status(EmailOutboxEntity.Status.PENDING)
                .attemptCount(0)
                .build();

        repo.save(entity); // JPA @PrePersist rellenará createdAt/updatedAt/nextAttemptAt
        log.info("[outbox] queued email id={} to='{}' subj='{}' html={}",
                entity.getId(), toCsv, safe(entity.getSubject()), entity.isHtml());
    }

    private static String safe(String s) {
        if (s == null) return "";
        return s.length() > 120 ? s.substring(0, 117) + "..." : s;
    }
}
