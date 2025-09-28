package com.aredondocharro.ClothingStore.notification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter @Setter
@ConfigurationProperties(prefix = "app.mail")
public class AppMailProperties {
    private String defaultFrom;
    private String defaultReplyTo;
    private String templatePrefix = "templates/email/";
    private String templateSuffix = ".html";
    private boolean templateCache = true;
}
