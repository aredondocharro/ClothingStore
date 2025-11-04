// src/main/java/.../identity/infrastructure/config/events/IdentityEventsConfig.java
package com.aredondocharro.ClothingStore.identity.infrastructure.config.events;

import com.aredondocharro.ClothingStore.identity.application.PublishVerificationEmailOnUserRegisteredService;
import com.aredondocharro.ClothingStore.identity.domain.port.out.*;
import com.aredondocharro.ClothingStore.shared.domain.event.EventBusPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class IdentityEventsConfig {

    @Bean
    public PublishVerificationEmailOnUserRegisteredService publishVerificationEmailOnUserRegisteredService(
            TokenGeneratorPort tokens, LoadUserPort loadUsers, EventBusPort eventBus,
            Clock clock, @Value("${app.verify.baseUrl}") String verifyBaseUrl) {
        return new PublishVerificationEmailOnUserRegisteredService(tokens, loadUsers, eventBus, clock, verifyBaseUrl);
    }
}
