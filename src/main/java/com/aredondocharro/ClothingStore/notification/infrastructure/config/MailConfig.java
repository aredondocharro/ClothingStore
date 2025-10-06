// src/main/java/com/aredondocharro/ClothingStore/notification/config/MailConfig.java
package com.aredondocharro.ClothingStore.notification.infrastructure.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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

    @Bean("emailTemplateResolver")
    public ClassLoaderTemplateResolver emailTemplateResolver(AppMailProperties props) {
        var r = new ClassLoaderTemplateResolver();
        r.setPrefix(props.getTemplatePrefix());  // "templates/email/"
        r.setSuffix(props.getTemplateSuffix());  // ".html"
        r.setTemplateMode(TemplateMode.HTML);
        r.setCharacterEncoding("UTF-8");
        r.setCacheable(false);                   // en dev: false
        r.setCheckExistence(true);
        return r;
    }

    @Bean("emailMessageSource")
    public ReloadableResourceBundleMessageSource emailMessageSource() {
        var ms = new ReloadableResourceBundleMessageSource();

        // SIN "classpath:" y SIN extensiones; uno por bundle
        ms.setBasenames(
                "classpath:i18n/email-verify",
                "classpath:i18n/email-password-reset",
                "classpath:i18n/email-common"
        );

        ms.setDefaultEncoding("UTF-8");
        ms.setFallbackToSystemLocale(false);

        // MUY IMPORTANTE: que NO devuelva el código si falta la clave
        ms.setUseCodeAsDefaultMessage(false);

        // opcional en dev
        ms.setCacheSeconds(3);
        return ms;
    }

    @Bean("emailTemplateEngine")
    public SpringTemplateEngine emailTemplateEngine(
            @Qualifier("emailTemplateResolver") ClassLoaderTemplateResolver resolver,
            @Qualifier("emailMessageSource") MessageSource emailMessageSource) {

        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);

        // ¡ENLAZA EL MS DE EMAIL AL ENGINE!
        engine.setTemplateEngineMessageSource(emailMessageSource);

        engine.setEnableSpringELCompiler(true);
        return engine;
    }
}

