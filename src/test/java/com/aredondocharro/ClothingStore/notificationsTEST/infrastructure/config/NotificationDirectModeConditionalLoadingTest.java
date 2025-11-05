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
        // Direct por defecto, pero lo explicitamos para claridad
        "app.mail.mode=direct",
        "spring.task.scheduling.enabled=false"
})
class NotificationDirectModeConditionalLoadingTest {

    @Autowired(required = false)
    OutboxEmailSenderAdapter outboxSender;

    @Autowired(required = false)
    OutboxEmailDispatcher dispatcher;

    @Autowired(required = false)
    SmtpEmailSenderAdapter smtpSender;

    @Test
    void whenModeDirect_thenSmtpPresent_andOutboxBeansAbsent() {
        assertThat(smtpSender).as("SMTP direct sender should be loaded").isNotNull();
        assertThat(outboxSender).as("Outbox sender must NOT be loaded in direct mode").isNull();
        assertThat(dispatcher).as("Outbox dispatcher must NOT be loaded in direct mode").isNull();
    }
}
