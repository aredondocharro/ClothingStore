package com.aredondocharro.ClothingStore.notificationsTEST.infrastructure.config;

import com.aredondocharro.ClothingStore.notification.infrastructure.out.outbox.OutboxEmailDispatcher;
import com.aredondocharro.ClothingStore.notification.infrastructure.out.outbox.OutboxEmailSenderAdapter;
import com.aredondocharro.ClothingStore.notification.infrastructure.out.smtp.SmtpEmailSenderAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "app.mail.mode=outbox",
        // Evitar hilos reales del scheduler en test
        "spring.task.scheduling.enabled=false"
})
class NotificationOutboxConditionalLoadingTest {

    @Autowired(required = false)
    OutboxEmailSenderAdapter outboxSender;

    @Autowired(required = false)
    OutboxEmailDispatcher dispatcher;

    @Autowired(required = false)
    SmtpEmailSenderAdapter smtpSender;

    @Test
    void whenModeOutbox_thenOutboxBeansPresent_andSmtpAbsent() {
        assertThat(outboxSender).as("Outbox sender should be loaded").isNotNull();
        assertThat(dispatcher).as("Outbox dispatcher should be loaded").isNotNull();
        assertThat(smtpSender).as("SMTP direct sender must NOT be loaded in outbox mode").isNull();
    }
}