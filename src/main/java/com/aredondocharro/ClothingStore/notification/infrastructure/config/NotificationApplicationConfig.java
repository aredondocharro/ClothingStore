// notification/infrastructure/config/NotificationApplicationConfig.java
package com.aredondocharro.ClothingStore.notification.infrastructure.config;

import com.aredondocharro.ClothingStore.notification.application.SendEmailOnPasswordResetRequested;
import com.aredondocharro.ClothingStore.notification.application.SendEmailOnVerificationRequested;
import com.aredondocharro.ClothingStore.notification.application.SendEmailService;
import com.aredondocharro.ClothingStore.notification.domain.port.in.SendEmailUseCase;
import com.aredondocharro.ClothingStore.notification.domain.port.out.EmailSenderPort;
import com.aredondocharro.ClothingStore.notification.domain.port.out.TemplateRendererPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationApplicationConfig {

    @Bean
    SendEmailService sendEmailService(TemplateRendererPort renderer, EmailSenderPort emailSender) {
        return new SendEmailService(renderer, emailSender);
    }

    @Bean
    public SendEmailOnPasswordResetRequested sendEmailOnPasswordResetRequested(SendEmailUseCase sendEmail) {
        return new SendEmailOnPasswordResetRequested(sendEmail);
    }

    @Bean
    public SendEmailOnVerificationRequested sendEmailOnVerificationRequested(SendEmailUseCase sendEmail) {
        return new SendEmailOnVerificationRequested(sendEmail);
    }
}
