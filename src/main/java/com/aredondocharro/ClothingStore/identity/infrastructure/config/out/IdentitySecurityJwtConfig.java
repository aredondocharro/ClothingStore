// src/main/java/.../identity/infrastructure/config/out/IdentitySecurityJwtConfig.java
package com.aredondocharro.ClothingStore.identity.infrastructure.config.out;

import com.aredondocharro.ClothingStore.identity.domain.port.out.*;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt.*;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt.JwtVerificationTokenRotationAdapter;
import com.aredondocharro.ClothingStore.identity.domain.port.out.VerificationTokenStorePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class IdentitySecurityJwtConfig {

    @Bean
    public RefreshTokenVerifierPort refreshTokenVerifierPort(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.issuer}") String issuer) {
        return new JwtRefreshTokenVerifierAdapter(secret, issuer);
    }

    @Bean
    public VerificationTokenPort verificationTokenPort(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.issuer}") String issuer) {
        return new JwtVerificationAdapter(secret, issuer);
    }

    @Bean
    public TokenGeneratorPort tokenGenerator(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.issuer}") String issuer,
            @Value("${security.jwt.access.seconds}") long accessSeconds,
            @Value("${security.jwt.refresh.seconds}") long refreshSeconds,
            @Value("${security.jwt.verify.seconds}") long verificationSeconds) {
        return new JwtTokenGeneratorAdapter(secret, issuer, accessSeconds, refreshSeconds, verificationSeconds);
    }
    @Bean
    public VerificationTokenRotationPort verificationTokenRotationPort(
            TokenGeneratorPort tokenGenerator,
            VerificationTokenStorePort verificationTokenStorePort,
            Clock clock
    ) {
        return new JwtVerificationTokenRotationAdapter(tokenGenerator, verificationTokenStorePort, clock);
    }

}
