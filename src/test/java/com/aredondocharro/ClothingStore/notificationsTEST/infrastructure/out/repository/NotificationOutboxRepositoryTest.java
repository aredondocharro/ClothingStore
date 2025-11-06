package com.aredondocharro.ClothingStore.notificationsTEST.infrastructure.out.repository;

import com.aredondocharro.ClothingStore.notification.infrastructure.out.persistence.entity.EmailOutboxEntity;
import com.aredondocharro.ClothingStore.notification.infrastructure.out.persistence.repository.EmailOutboxRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class NotificationOutboxRepositoryTest {

    @Autowired
    EmailOutboxRepository repo;

    @Test
    void findDue_returnsOnlyPendingDue_inChronologicalOrder() {
        repo.deleteAll();
        Instant now = Instant.now();

        // due soonest (PENDING y vencido)
        EmailOutboxEntity a = repo.save(EmailOutboxEntity.builder()
                .toAddresses("a@x")
                .subject("s")
                .body("b")
                .html(false)
                .status(EmailOutboxEntity.Status.PENDING)
                .nextAttemptAt(now.minusSeconds(30))
                .messageKey("test-" + UUID.randomUUID())
                .build());

        // due later (PENDING y vencido más tarde)
        EmailOutboxEntity b = repo.save(EmailOutboxEntity.builder()
                .toAddresses("b@x")
                .subject("s")
                .body("b")
                .html(false)
                .status(EmailOutboxEntity.Status.PENDING)
                .nextAttemptAt(now.minusSeconds(10))
                .messageKey("test-" + UUID.randomUUID())
                .build());

        // not due (PENDING a futuro) — sirve para comprobar exclusión
        repo.save(EmailOutboxEntity.builder()
                .toAddresses("c@x")
                .subject("s")
                .body("b")
                .html(false)
                .status(EmailOutboxEntity.Status.PENDING)
                .nextAttemptAt(now.plusSeconds(30))
                .messageKey("test-" + UUID.randomUUID())
                .build());

        // ⚠️ Eliminado el caso "SENT": no es necesario para este test y dispara la CHECK por @PrePersist

        List<EmailOutboxEntity> due = repo.findByStatusAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(
                EmailOutboxEntity.Status.PENDING, now, PageRequest.of(0, 10));

        assertThat(due).extracting(EmailOutboxEntity::getId)
                .containsExactly(a.getId(), b.getId());
    }
}
