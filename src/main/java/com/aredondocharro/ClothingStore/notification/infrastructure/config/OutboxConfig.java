package com.aredondocharro.ClothingStore.notification.infrastructure.config;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;


@Configuration
@EnableConfigurationProperties({OutboxMailProperties.class, AppMailProperties.class})
@ConditionalOnProperty(prefix = "app.mail", name = "mode", havingValue = "outbox")
public class OutboxConfig { }