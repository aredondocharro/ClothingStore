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

        // due soonest
        EmailOutboxEntity a = repo.save(EmailOutboxEntity.builder()
                .toAddresses("a@x")
                .subject("s")
                .body("b")
                .nextAttemptAt(now.minusSeconds(30))
                .status(EmailOutboxEntity.Status.PENDING)
                .html(false)
                .build());

        // due later
        EmailOutboxEntity b = repo.save(EmailOutboxEntity.builder()
                .toAddresses("b@x")
                .subject("s")
                .body("b")
                .nextAttemptAt(now.minusSeconds(10))
                .status(EmailOutboxEntity.Status.PENDING)
                .html(false)
                .build());

        // not due (future)
        repo.save(EmailOutboxEntity.builder()
                .toAddresses("c@x")
                .subject("s")
                .body("b")
                .nextAttemptAt(now.plusSeconds(30))
                .status(EmailOutboxEntity.Status.PENDING)
                .html(false)
                .build());

        // not pending
        repo.save(EmailOutboxEntity.builder()
                .toAddresses("d@x")
                .subject("s")
                .body("b")
                .nextAttemptAt(now.minusSeconds(5))
                .status(EmailOutboxEntity.Status.SENT)
                .html(false)
                .build());

        List<EmailOutboxEntity> due = repo.findByStatusAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(
                EmailOutboxEntity.Status.PENDING, now, PageRequest.of(0, 10));

        assertThat(due).extracting(EmailOutboxEntity::getId)
                .containsExactly(a.getId(), b.getId());
    }
}
