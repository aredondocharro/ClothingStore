// src/main/java/com/aredondocharro/ClothingStore/notification/config/AppMailProperties.java
package com.aredondocharro.ClothingStore.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public class AppMailProperties {

    private String defaultFrom;
    private String defaultReplyTo;
    private boolean templateCache = true;
    private String templatePrefix = "templates/email/";
    private String templateSuffix = ".html";

    public String getDefaultFrom() {
        return defaultFrom;
    }

    public void setDefaultFrom(String defaultFrom) {
        this.defaultFrom = defaultFrom;
    }

    public String getDefaultReplyTo() {
        return defaultReplyTo;
    }

    public void setDefaultReplyTo(String defaultReplyTo) {
        this.defaultReplyTo = defaultReplyTo;
    }

    public boolean isTemplateCache() {
        return templateCache;
    }

    public void setTemplateCache(boolean templateCache) {
        this.templateCache = templateCache;
    }

    public String getTemplatePrefix() {
        return templatePrefix;
    }

    public void setTemplatePrefix(String templatePrefix) {
        this.templatePrefix = templatePrefix;
    }

    public String getTemplateSuffix() {
        return templateSuffix;
    }

    public void setTemplateSuffix(String templateSuffix) {
        this.templateSuffix = templateSuffix;
    }
}
