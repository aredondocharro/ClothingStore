package com.aredondocharro.ClothingStore.notification.infrastructure.out.outbox;

import com.aredondocharro.ClothingStore.notification.infrastructure.config.AppMailProperties;
import com.aredondocharro.ClothingStore.notification.infrastructure.config.OutboxMailProperties;
import com.aredondocharro.ClothingStore.notification.infrastructure.out.persistence.entity.EmailOutboxEntity;
import com.aredondocharro.ClothingStore.notification.infrastructure.out.persistence.repository.EmailOutboxRepository;
import com.aredondocharro.ClothingStore.shared.log.LogSanitizer;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.mail", name = "mode", havingValue = "outbox")
public class OutboxEmailDispatcher {

    private final EmailOutboxRepository repo;
    private final JavaMailSender mailSender;
    private final AppMailProperties appProps;
    private final OutboxMailProperties outProps;
    private final TransactionTemplate tx;

    @Scheduled(
            fixedDelayString = "${app.mail.outbox.poll-interval:2s}",
            initialDelayString = "${app.mail.outbox.initial-delay:0s}"
    )
    public void tick() {
        final Instant now = Instant.now();
        final int pageSize = outProps.getBatchSize();

        List<EmailOutboxEntity> due = repo.findByStatusAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(
                EmailOutboxEntity.Status.PENDING, now, PageRequest.of(0, pageSize));

        if (due.isEmpty()) return;

        log.debug("[outbox] {} messages due (<= {})", due.size(), now);

        for (EmailOutboxEntity e : due) {
            try {
                // AÍSLA cada mensaje en SU PROPIA TRANSACCIÓN
                tx.executeWithoutResult(status -> processOne(e.getId()));
            } catch (Exception ex) {
                // Nunca caemos el scheduler por un fallo de un item
                log.warn("[outbox] process failed for id={}: {}", e.getId(), trim(ex.getMessage()));
            }
        }
    }

    void processOne(Long id) {
        EmailOutboxEntity e = repo.findById(id).orElse(null);
        if (e == null || e.getStatus() != EmailOutboxEntity.Status.PENDING) return;

        try {
            e.setStatus(EmailOutboxEntity.Status.PROCESSING);
            repo.saveAndFlush(e); // puede lanzar lock exception si otro worker lo tomó
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException lock) {
            log.debug("[outbox] id={} claimed by another worker, skipping", id);
            return;
        }

        try {
            doSend(e);
            e.setStatus(EmailOutboxEntity.Status.SENT);
            e.setSentAt(Instant.now());
            e.setLastError(null);
            e.setNextAttemptAt(null); // OK con la migración
            repo.save(e);
            log.info("[outbox] SENT id={} to='{}' subj='{}'", e.getId(), LogSanitizer.maskEmail(e.getToAddresses()), e.getSubject());
        } catch (Exception ex) {
            int attempts = e.getAttemptCount() + 1;
            e.setAttemptCount(attempts);
            e.setLastAttemptAt(Instant.now());
            e.setLastError(trim(ex.getMessage()));
            if (attempts >= outProps.getMaxAttempts()) {
                e.setStatus(EmailOutboxEntity.Status.FAILED);
                e.setNextAttemptAt(null);
            } else {
                e.setStatus(EmailOutboxEntity.Status.PENDING);
                e.setNextAttemptAt(nextRetryInstant(attempts));
            }
            repo.save(e);
            log.warn("[outbox] {} id={} attempts={} err='{}'",
                    e.getStatus(), e.getId(), attempts, e.getLastError());
        }
    }

    private void doSend(EmailOutboxEntity e) throws Exception {
        String[] tos = splitCsv(e.getToAddresses());

        if (!e.isHtml()) {
            SimpleMailMessage msg = new SimpleMailMessage();
            if (appProps.getDefaultFrom() != null) msg.setFrom(appProps.getDefaultFrom());
            if (appProps.getDefaultReplyTo() != null) msg.setReplyTo(appProps.getDefaultReplyTo());
            msg.setTo(tos);
            msg.setSubject(e.getSubject());
            msg.setText(e.getBody());
            mailSender.send(msg);
            return;
        }

        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper h = new MimeMessageHelper(mime, "UTF-8");
        if (appProps.getDefaultFrom() != null) h.setFrom(appProps.getDefaultFrom());
        if (appProps.getDefaultReplyTo() != null) h.setReplyTo(appProps.getDefaultReplyTo());
        h.setTo(tos);
        h.setSubject(e.getSubject());
        h.setText(e.getBody(), true);
        mailSender.send(mime);
    }

    private Instant nextRetryInstant(int attempts) {
        Duration base = outProps.getBaseDelay();
        double mult = Math.max(1.0, outProps.getMultiplier());
        long delayMillis = (long) (base.toMillis() * Math.pow(mult, Math.max(0, attempts - 1)));
        return Instant.now().plusMillis(delayMillis);
    }

    private static String[] splitCsv(String csv) {
        if (csv == null || csv.isBlank()) return new String[0];
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    private static String trim(String s) {
        if (s == null) return null;
        return s.length() > 200 ? s.substring(0, 197) + "..." : s;
    }
}
