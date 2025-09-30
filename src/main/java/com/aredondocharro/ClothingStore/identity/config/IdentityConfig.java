package com.aredondocharro.ClothingStore.identity.config;

import com.aredondocharro.ClothingStore.identity.infrastructure.out.crypto.BCryptPasswordHasherAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt.JwtTokenGeneratorAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt.JwtVerificationAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.UserJpaAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringDataUserRepository;
import com.aredondocharro.ClothingStore.identity.application.LoginService;
import com.aredondocharro.ClothingStore.identity.application.RegisterUserService;
import com.aredondocharro.ClothingStore.identity.application.VerifyEmailService;
import com.aredondocharro.ClothingStore.identity.domain.port.in.LoginUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.RegisterUserUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.VerifyEmailUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenGeneratorPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.VerificationTokenPort;
import com.aredondocharro.ClothingStore.notification.domain.port.in.SendEmailUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdentityConfig {

    @Bean
    BCryptPasswordHasherAdapter passwordHasher() {
        return new BCryptPasswordHasherAdapter();
    }

    @Bean
    TokenGeneratorPort tokenGenerator(@Value("${security.jwt.secret}") String secret,
                                      @Value("${security.jwt.issuer:clothing-store}") String issuer,
                                      @Value("${security.jwt.accessSeconds:900}") long accessSeconds,
                                      @Value("${security.jwt.refreshSeconds:1209600}") long refreshSeconds,
                                      @Value("${security.jwt.verificationSeconds:1800}") long verificationSeconds) {
        return new JwtTokenGeneratorAdapter(secret, issuer, accessSeconds, refreshSeconds, verificationSeconds);
    }

    @Bean
    VerificationTokenPort verificationTokenPort(@Value("${security.jwt.secret}") String secret,
                                                @Value("${security.jwt.issuer:clothing-store}") String issuer) {
        return new JwtVerificationAdapter(secret, issuer);
    }

    @Bean
    UserJpaAdapter userPersistence(SpringDataUserRepository repo) {
        return new UserJpaAdapter(repo);
    }

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserJpaAdapter persistence,
                                                   BCryptPasswordHasherAdapter hasher,
                                                   TokenGeneratorPort tokens,
                                                   SendEmailUseCase sendEmailUseCase,
                                                   @Value("${app.apiBaseUrl:http://localhost:8081}") String apiBaseUrl) {
        var mailer = new com.aredondocharro.ClothingStore.identity.infrastructure.out.mail.MailerAdapter(sendEmailUseCase);
        return new RegisterUserService(persistence, persistence, hasher, tokens, mailer, normalizeBase(apiBaseUrl));
    }

    private String normalizeBase(String base) {
        return base != null && base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }


    @Bean
    LoginUseCase loginUseCase(UserJpaAdapter persistence, BCryptPasswordHasherAdapter hasher,
                              TokenGeneratorPort tokens) {
        return new LoginService(persistence, hasher, tokens);
    }

@Bean
    public VerifyEmailUseCase verifyEmailUseCase(VerificationTokenPort verifier,
                                                 UserJpaAdapter persistence,
                                                 TokenGeneratorPort tokens) {
        return new VerifyEmailService(verifier, persistence, persistence, tokens);
    }
}
