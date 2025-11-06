package com.aredondocharro.ClothingStore.notification.infrastructure.out.outbox;

import com.aredondocharro.ClothingStore.notification.domain.model.EmailAddress;
import com.aredondocharro.ClothingStore.notification.domain.model.EmailMessage;
import com.aredondocharro.ClothingStore.notification.domain.port.out.EmailSenderPort;
import com.aredondocharro.ClothingStore.notification.infrastructure.out.persistence.entity.EmailOutboxEntity;
import com.aredondocharro.ClothingStore.notification.infrastructure.out.persistence.repository.EmailOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.mail", name = "mode", havingValue = "outbox")
public class OutboxEmailSenderAdapter implements EmailSenderPort {

    private final EmailOutboxRepository repo;
    private final PlatformTransactionManager txManager; // <-- NUEVO

    @Override
    public void send(EmailMessage message) {
        final String toCsv = message.to().stream()
                .map(EmailAddress::value)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.joining(","));

        final String subj = message.subject();
        final String body = message.body();
        final boolean html = message.html();

        final String messageKey = sha256Hex("v1|" + toCsv + "|" + subj + "|" + body);

        // TX NUEVA explícita
        TransactionTemplate tpl = new TransactionTemplate(txManager);
        tpl.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        tpl.executeWithoutResult(status -> {
            try {
                EmailOutboxEntity entity = EmailOutboxEntity.builder()
                        .toAddresses(toCsv)
                        .subject(subj)
                        .body(body)
                        .html(html)
                        .messageKey(messageKey)
                        .status(EmailOutboxEntity.Status.PENDING)
                        .attemptCount(0)
                        .build();

                repo.saveAndFlush(entity); // flush inmediato dentro de la TX hija
                log.info("[outbox] queued email id={} key={} to='{}' subj='{}' html={}",
                        entity.getId(), messageKey, toCsv, safe(entity.getSubject()), entity.isHtml());

            } catch (DataIntegrityViolationException dup) {
                // Duplicado lógico: marcamos rollback para limpiar el contexto hijo y no dejar "entidades colgadas"
                status.setRollbackOnly();
                log.info("[outbox] duplicate ignored key={} to='{}' subj='{}'", messageKey, toCsv, safe(subj));
            }
        });
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(out.length * 2);
            for (byte b : out) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String safe(String s) {
        if (s == null) return "";
        return s.length() > 120 ? s.substring(0, 117) + "..." : s;
    }
}
