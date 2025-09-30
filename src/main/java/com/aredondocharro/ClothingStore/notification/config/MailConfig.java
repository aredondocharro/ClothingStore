// src/main/java/com/aredondocharro/ClothingStore/notification/config/MailConfig.java
package com.aredondocharro.ClothingStore.notification.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
@EnableConfigurationProperties(AppMailProperties.class)
public class MailConfig {

    @Bean(name = "emailTemplateResolver")
    public ClassLoaderTemplateResolver emailTemplateResolver(AppMailProperties props) {
        ClassLoaderTemplateResolver r = new ClassLoaderTemplateResolver();
        r.setPrefix(props.getTemplatePrefix());   // p.ej. "templates/email/"
        r.setSuffix(props.getTemplateSuffix());   // p.ej. ".html"
        r.setTemplateMode(TemplateMode.HTML);
        r.setCharacterEncoding("UTF-8");
        r.setCacheable(props.isTemplateCache());
        r.setCheckExistence(true);
        r.setOrder(1);
        return r;
    }

    @Bean(name = "emailMessageSource")
    public MessageSource emailMessageSource() {
        ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
        ms.setBasenames(
                "classpath:i18n/messages",       // genérico si lo necesitas
                "classpath:i18n/email-common",   // brand, footer, etc.
                "classpath:i18n/email-verify",   // verificación
                "classpath:i18n/email-order"     // (cuando lo añadas)
        );
        ms.setDefaultEncoding("UTF-8");
        ms.setFallbackToSystemLocale(false);
        ms.setCacheSeconds(5); // hot-reload en dev
        return ms;
    }

    @Bean(name = "emailTemplateEngine")
    public SpringTemplateEngine emailTemplateEngine(
            @Qualifier("emailTemplateResolver") ClassLoaderTemplateResolver resolver,
            @Qualifier("emailMessageSource") MessageSource emailMessageSource) {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        engine.setTemplateEngineMessageSource(emailMessageSource);
        return engine;
    }
}
