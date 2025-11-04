// src/main/java/.../identity/infrastructure/config/out/IdentityAdaptersPersistenceConfig.java
package com.aredondocharro.ClothingStore.identity.infrastructure.config.out;

import com.aredondocharro.ClothingStore.identity.domain.port.out.*;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.*;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.mapper.*;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class IdentityAdaptersPersistenceConfig {

    @Bean
    public UserPersistenceAdapter userPersistenceAdapter(SpringDataUserRepository repo, UserMapper mapper) {
        return new UserPersistenceAdapter(repo, mapper);
    }

    @Bean
    public UserRepositoryPort userRepositoryPort(SpringDataUserRepository repo, UserMapper mapper) {
        return new UserRepositoryAdapter(repo, mapper);
    }

    @Bean
    public PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort(
            SpringPasswordResetTokenJpaRepository jpa, PasswordResetTokenMapper mapper) {
        return new PasswordResetTokenRepositoryAdapter(jpa, mapper);
    }

    @Bean
    public RefreshTokenStorePort refreshTokenStorePort(
            SpringDataRefreshSessionRepository repo, RefreshSessionEntityMapper mapper) {
        return new JpaRefreshTokenStoreAdapter(repo, mapper);
    }

    @Bean
    public UserAdminRepositoryPort userAdminRepositoryPort(SpringDataUserRepository repo) {
        return new UserAdminRepositoryAdapter(repo);
    }
}
