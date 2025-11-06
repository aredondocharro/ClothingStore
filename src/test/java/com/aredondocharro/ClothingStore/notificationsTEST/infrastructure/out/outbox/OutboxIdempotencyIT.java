package com.aredondocharro.ClothingStore.notificationsTEST.infrastructure.out.outbox;

import com.aredondocharro.ClothingStore.notification.domain.model.EmailAddress;
import com.aredondocharro.ClothingStore.notification.domain.model.EmailMessage;
import com.aredondocharro.ClothingStore.notification.infrastructure.out.outbox.OutboxEmailSenderAdapter;
import com.aredondocharro.ClothingStore.notification.infrastructure.out.persistence.entity.EmailOutboxEntity;
import com.aredondocharro.ClothingStore.notification.infrastructure.out.persistence.repository.EmailOutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@Testcontainers
@ActiveProfiles("test")
@Import(OutboxEmailSenderAdapter.class)
@TestPropertySource(properties = "app.mail.mode=outbox")
class OutboxIdempotencyIT {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.flyway.enabled", () -> true);
        r.add("spring.flyway.locations", () -> "classpath:db/migration");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired EmailOutboxRepository repo;
    @Autowired OutboxEmailSenderAdapter adapter;

    @BeforeEach void clean() { repo.deleteAll(); }

    @Test
    void sameToSubjectBody_enqueuesOnlyOnce() {
        var msg = new EmailMessage(null,
                List.of(new EmailAddress("user@example.com")),
                "Verify your email",
                "Hi! Click this link: https://app/verify?token=abc123",
                true);

        adapter.send(msg);
        adapter.send(msg);

        assertThat(repo.count()).isEqualTo(1);
    }

    @Test
    void differentBody_enqueuesTwice() {
        var a = new EmailMessage(null,
                List.of(new EmailAddress("user@example.com")),
                "Reset your password",
                "Click: https://app/reset?token=AAA", true);
        var b = new EmailMessage(null,
                List.of(new EmailAddress("user@example.com")),
                "Reset your password",
                "Click: https://app/reset?token=BBB", true);

        adapter.send(a);
        adapter.send(b);

        assertThat(repo.count()).isEqualTo(2);
    }
}

