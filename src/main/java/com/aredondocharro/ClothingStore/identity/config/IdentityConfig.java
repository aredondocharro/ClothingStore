package com.aredondocharro.ClothingStore.identity.config;

import com.aredondocharro.ClothingStore.identity.application.LoginService;
import com.aredondocharro.ClothingStore.identity.application.LogoutService;
import com.aredondocharro.ClothingStore.identity.application.RefreshAccessTokenService;
import com.aredondocharro.ClothingStore.identity.application.RegisterUserService;
import com.aredondocharro.ClothingStore.identity.application.VerifyEmailService;
import com.aredondocharro.ClothingStore.identity.domain.port.in.LoginUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.LogoutUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.RefreshAccessTokenUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.RegisterUserUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.in.VerifyEmailUseCase;
import com.aredondocharro.ClothingStore.identity.domain.port.out.LoadUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.MailerPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordHasherPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.RefreshTokenStorePort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SaveUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenGeneratorPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenVerifierPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.VerificationTokenPort;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.crypto.BCryptPasswordHasherAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt.JwtTokenGeneratorAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt.JwtTokenVerifierAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt.JwtVerificationAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.mail.MailerAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.JpaRefreshTokenStoreAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.UserJpaAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringDataRefreshSessionRepository;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringDataUserRepository;
import com.aredondocharro.ClothingStore.notification.domain.port.in.SendEmailUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wiring de Identity siguiendo estilo Hexagonal:
 * - Adapters OUT
 * - Seguridad / JWT
 * - Use cases IN
 */
@Configuration
public class IdentityConfig {

    // ========================================================================
    // Adapters (OUT)
    // ========================================================================

    /** Adaptador JPA que implementa LoadUserPort / SaveUserPort */
    @Bean
    public UserJpaAdapter userJpaAdapter(SpringDataUserRepository repo) {
        return new UserJpaAdapter(repo);
    }

    /** Hasher de contraseñas (BCrypt) */
    @Bean
    public PasswordHasherPort passwordHasherPort() {
        return new BCryptPasswordHasherAdapter();
    }

    /** Envío de correos usando el caso de uso de notificaciones */
    @Bean
    public MailerPort mailerPort(SendEmailUseCase sendEmail) {
        return new MailerAdapter(sendEmail);
    }

    /** Persistencia de sesiones/refresh tokens (JPA) */
    @Bean
    public RefreshTokenStorePort refreshTokenStorePort(SpringDataRefreshSessionRepository repo) {
        return new JpaRefreshTokenStoreAdapter(repo);
    }

    // ========================================================================
    // Seguridad / JWT (Ports OUT)
    // ========================================================================

    /** Verificación de tokens para flujos como verify-email */
    @Bean
    public VerificationTokenPort verificationTokenPort(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.issuer}") String issuer
    ) {
        return new JwtVerificationAdapter(secret, issuer);
    }

    /** Verificador de JWT (access/refresh) */
    @Bean
    public TokenVerifierPort tokenVerifierPort(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.issuer}") String issuer
    ) {
        return new JwtTokenVerifierAdapter(secret, issuer);
    }

    /** Generador de JWTs (access/refresh/verify) */
    @Bean
    public TokenGeneratorPort tokenGenerator(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.issuer}") String issuer,
            @Value("${security.jwt.access.seconds}") long accessSeconds,
            @Value("${security.jwt.refresh.seconds}") long refreshSeconds,
            @Value("${security.jwt.verify.seconds}") long verificationSeconds
    ) {
        return new JwtTokenGeneratorAdapter(secret, issuer, accessSeconds, refreshSeconds, verificationSeconds);
    }

    // ========================================================================
    // Use cases (IN)
    // ========================================================================

    /** Registro de usuario + email de verificación */
    @Bean
    public RegisterUserUseCase registerUserUseCase(
            LoadUserPort loadUserPort,        // satisfecho por userJpaAdapter
            SaveUserPort saveUserPort,        // satisfecho por userJpaAdapter
            PasswordHasherPort hasher,
            TokenGeneratorPort tokens,
            MailerPort mailer,
            @Value("${app.apiBaseUrl}") String apiBaseUrl
    ) {
        return new RegisterUserService(loadUserPort, saveUserPort, hasher, tokens, mailer, apiBaseUrl);
    }

    /** Login + emisión de access/refresh */
    @Bean
    public LoginUseCase loginUseCase(LoadUserPort loadUserPort,
                                     PasswordHasherPort hasher,
                                     TokenGeneratorPort tokens,
                                     RefreshTokenStorePort store,
                                     TokenVerifierPort verifier) {
        return new LoginService(loadUserPort, hasher, tokens, store, verifier);
    }

    /** Verificación de email */
    @Bean
    public VerifyEmailUseCase verifyEmailUseCase(VerificationTokenPort verifierToken,
                                                 LoadUserPort loadUserPort,
                                                 SaveUserPort saveUserPort,
                                                 TokenGeneratorPort tokens,
                                                 RefreshTokenStorePort store,
                                                 TokenVerifierPort verifier) {
        return new VerifyEmailService(verifierToken, loadUserPort, saveUserPort, tokens, store, verifier);
    }

    /** Refresh de access token a partir del refresh token */
    @Bean
    public RefreshAccessTokenUseCase refreshAccessTokenUseCase(
            TokenVerifierPort tokenVerifier,
            RefreshTokenStorePort store,
            LoadUserPort loadUserPort,
            TokenGeneratorPort tokens
    ) {
        return new RefreshAccessTokenService(tokenVerifier, store, loadUserPort, tokens);
    }

    /** Logout (revocar sesión/refresh) */
    @Bean
    public LogoutUseCase logoutUseCase(
            TokenVerifierPort tokenVerifier,
            RefreshTokenStorePort store
    ) {
        return new LogoutService(tokenVerifier, store);
    }
}

