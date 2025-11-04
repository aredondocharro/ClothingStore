// src/main/java/.../identity/infrastructure/in/config/IdentityWebJacksonConfig.java
package com.aredondocharro.ClothingStore.identity.infrastructure.config.in;

import com.fasterxml.jackson.databind.MapperFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class IdentityWebJacksonConfig {
    @Bean
    Jackson2ObjectMapperBuilderCustomizer caseInsensitiveEnums() {
        return builder -> builder.featuresToEnable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    }
}
