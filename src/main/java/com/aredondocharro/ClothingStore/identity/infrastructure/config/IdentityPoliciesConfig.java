// src/main/java/.../identity/infrastructure/config/IdentityPoliciesConfig.java
package com.aredondocharro.ClothingStore.identity.infrastructure.config;

import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordHasherPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordPolicyPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SessionManagerPort;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.crypto.BCryptPasswordHasherAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.policy.NoopSessionManager;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.policy.SimplePasswordPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class IdentityPoliciesConfig {

    @Bean public PasswordHasherPort passwordHasherPort() { return new BCryptPasswordHasherAdapter(); }
    @Bean public PasswordPolicyPort passwordPolicyPort() { return new SimplePasswordPolicy(); }
    @Bean public SessionManagerPort sessionManagerPort() { return new NoopSessionManager(); }
}
