package com.aredondocharro.ClothingStore.notification.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.mail.mode", havingValue = "outbox")
public class OutboxSchedulingConfig {}
