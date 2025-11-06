// src/test/java/com/aredondocharro/ClothingStore/notification/infrastructure/out/outbox/OutboxEmailDispatcherTest.java
package com.aredondocharro.ClothingStore.notification.infrastructure.out.outbox;

import com.aredondocharro.ClothingStore.notification.infrastructure.config.AppMailProperties;
import com.aredondocharro.ClothingStore.notification.infrastructure.config.OutboxMailProperties;
import com.aredondocharro.ClothingStore.notification.infrastructure.out.persistence.entity.EmailOutboxEntity;
import com.aredondocharro.ClothingStore.notification.infrastructure.out.persistence.repository.EmailOutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@DataJpaTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackageClasses = EmailOutboxEntity.class)
@EnableJpaRepositories(basePackageClasses = EmailOutboxRepository.class)
@Import({OutboxEmailDispatcher.class, OutboxEmailDispatcherTest.LocalProps.class})
@TestPropertySource(properties = {
        // DB y JPA (usamos DDL de Hibernate para este test de dispatcher)
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",

        // Habilitar el modo outbox del bean condicional
        "app.mail.mode=outbox",

        // Propiedades mínimas para el envío
        "app.mail.from=no-reply@test.local",

        // Config del outbox
        "app.mail.outbox.max-attempts=1",
        "app.mail.outbox.batch-size=50",
        "app.mail.outbox.base-delay=PT0S",
        "app.mail.outbox.multiplier=1.0"
})
class OutboxEmailDispatcherTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @TestConfiguration
    @EnableConfigurationProperties({AppMailProperties.class, OutboxMailProperties.class})
    static class LocalProps { }

    @Autowired
    EmailOutboxRepository repo;

    @Autowired
    OutboxEmailDispatcher dispatcher;

    @MockitoBean
    JavaMailSender mailSender;

    @BeforeEach
    void clean() {
        repo.deleteAll();
    }

    @Test
    void tick_sends_plain_text_and_marks_sent() {
        // given: registro due (PENDING y nextAttemptAt <= now)
        EmailOutboxEntity e = EmailOutboxEntity.builder()
                .toAddresses("dev@example.com")
                .subject("Hello")
                .body("Body")
                .html(false) // fuerza rama SimpleMailMessage
                .status(EmailOutboxEntity.Status.PENDING)
                .nextAttemptAt(Instant.now().minusSeconds(1))
                .messageKey("test-" + UUID.randomUUID()) // <-- clave requerida
                .build();
        e = repo.saveAndFlush(e);

        // when
        dispatcher.tick();

        // then: SMTP llamado con los datos correctos
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getTo()).containsExactly("dev@example.com");
        assertThat(captor.getValue().getSubject()).isEqualTo("Hello");
        assertThat(captor.getValue().getText()).isEqualTo("Body");

        // then: fila actualizada a SENT
        EmailOutboxEntity saved = repo.findById(e.getId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(EmailOutboxEntity.Status.SENT);
        assertThat(saved.getAttemptCount()).isEqualTo(0);
        assertThat(saved.getSentAt()).isNotNull();
        assertThat(saved.getLastError()).isNull();
        assertThat(saved.getNextAttemptAt()).isNull();
    }

    @Test
    void tick_marks_failed_when_mail_sender_throws_and_no_retry_with_max_attempts_1() {
        // given
        EmailOutboxEntity e = EmailOutboxEntity.builder()
                .toAddresses("dev@example.com")
                .subject("Hello")
                .body("Body")
                .html(false)
                .status(EmailOutboxEntity.Status.PENDING)
                .nextAttemptAt(Instant.now().minusSeconds(1))
                .messageKey("test-" + UUID.randomUUID()) // <-- clave requerida
                .build();
        e = repo.saveAndFlush(e);

        // fallo en el envío
        doThrow(new RuntimeException("SMTP down"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // when
        dispatcher.tick();

        // then: fila marcada como FAILED (maxAttempts=1)
        EmailOutboxEntity saved = repo.findById(e.getId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(EmailOutboxEntity.Status.FAILED);
        assertThat(saved.getAttemptCount()).isEqualTo(1);
        assertThat(saved.getLastAttemptAt()).isNotNull();
        assertThat(saved.getLastError()).containsIgnoringCase("SMTP down");
        assertThat(saved.getNextAttemptAt()).isNull(); // sin replanificación
    }
}
