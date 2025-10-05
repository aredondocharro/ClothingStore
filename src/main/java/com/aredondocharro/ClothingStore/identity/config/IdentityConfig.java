package com.aredondocharro.ClothingStore.identity.config;

import com.aredondocharro.ClothingStore.identity.application.LoginService;
import com.aredondocharro.ClothingStore.identity.application.LogoutService;
import com.aredondocharro.ClothingStore.identity.application.PasswordRecoveryService;
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
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordPolicyPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.PasswordResetTokenRepositoryPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.RefreshTokenStorePort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SaveUserPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.SessionManagerPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenGeneratorPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.TokenVerifierPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.UserRepositoryPort;
import com.aredondocharro.ClothingStore.identity.domain.port.out.VerificationTokenPort;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.crypto.BCryptPasswordHasherAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt.JwtTokenGeneratorAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt.JwtTokenVerifierAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt.JwtVerificationAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.mail.MailerAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.JpaRefreshTokenStoreAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.UserPersistenceAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.UserRepositoryAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.PasswordResetTokenRepositoryAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringDataRefreshSessionRepository;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringDataUserRepository;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringPasswordResetTokenJpa;
import com.aredondocharro.ClothingStore.identity.infrastructure.security.NoopSessionManager;
import com.aredondocharro.ClothingStore.identity.infrastructure.security.SimplePasswordPolicy;
import com.aredondocharro.ClothingStore.notification.domain.port.in.SendEmailUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdentityConfig {

    // ========================================================================
    // Adapters (OUT)
    // ========================================================================

    /** Adapter JPA que implementa LoadUserPort / SaveUserPort (dominio User) */
    @Bean
    public UserPersistenceAdapter userPersistenceAdapter(SpringDataUserRepository repo) {
        return new UserPersistenceAdapter(repo);
    }

    /** Adapter para UserRepositoryPort (UserView + updatePasswordHash) */
    @Bean
    public UserRepositoryPort userRepositoryPort(SpringDataUserRepository repo) {
        return new UserRepositoryAdapter(repo);
    }

    /** Adapter para PasswordResetTokenRepositoryPort (tokens de reset) */
    @Bean
    public PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort(SpringPasswordResetTokenJpa jpa) {
        return new PasswordResetTokenRepositoryAdapter(jpa);
    }

    /** Hasher de contraseñas (BCrypt) */
    @Bean
    public PasswordHasherPort passwordHasherPort() {
        return new BCryptPasswordHasherAdapter();
    }

    /** Envío de correos usando el caso de uso de notificaciones */
    @Bean
    public MailerPort mailerPort(SendEmailUseCase sendEmail,
                                 @Value("${mail.from:no-reply@clothingstore.local}") String from,
                                 @Value("${mail.templates.verify-email:verify-email}") String verifyTpl,
                                 @Value("${mail.templates.password-reset:password-reset}") String resetTpl) {
        return new MailerAdapter(sendEmail, from, verifyTpl, resetTpl);
    }

    /** Persistencia de sesiones/refresh tokens (JPA) */
    @Bean
    public RefreshTokenStorePort refreshTokenStorePort(SpringDataRefreshSessionRepository repo) {
        return new JpaRefreshTokenStoreAdapter(repo);
    }

    // ========================================================================
    // Seguridad / JWT (Ports OUT)
    // ========================================================================

    @Bean
    public VerificationTokenPort verificationTokenPort(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.issuer}") String issuer
    ) {
        return new JwtVerificationAdapter(secret, issuer);
    }

    @Bean
    public TokenVerifierPort tokenVerifierPort(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.issuer}") String issuer
    ) {
        return new JwtTokenVerifierAdapter(secret, issuer);
    }

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
    // Password recovery module (Policy, Sessions, Service)
    // ========================================================================

    @Bean
    public PasswordPolicyPort passwordPolicyPort() {
        return new SimplePasswordPolicy();
    }

    @Bean
    public SessionManagerPort sessionManagerPort() {
        return new NoopSessionManager();
    }

    @Bean
    public PasswordRecoveryService passwordRecoveryService(
            UserRepositoryPort users,
            PasswordResetTokenRepositoryPort tokens,
            PasswordPolicyPort passwordPolicy,
            MailerPort mailer,
            SessionManagerPort sessions,
            PasswordHasherPort passwordHasher,
            @Value("${app.reset.baseUrl}") String resetBaseUrl
    ) {
        return new PasswordRecoveryService(users, tokens, passwordPolicy, mailer, sessions, passwordHasher, resetBaseUrl);
    }

    // ========================================================================
    // Use cases (IN)
    // ========================================================================

    @Bean
    public RegisterUserUseCase registerUserUseCase(
            LoadUserPort loadUserPort,        // satisfecho por userPersistenceAdapter
            SaveUserPort saveUserPort,        // satisfecho por userPersistenceAdapter
            PasswordHasherPort hasher,
            TokenGeneratorPort tokens,
            MailerPort mailer,
            @Value("${app.verify.baseUrl}") String verifyBaseUrl
    ) {
        return new RegisterUserService(loadUserPort, saveUserPort, hasher, tokens, mailer, verifyBaseUrl);
    }

    @Bean
    public LoginUseCase loginUseCase(LoadUserPort loadUserPort,
                                     PasswordHasherPort hasher,
                                     TokenGeneratorPort tokens,
                                     RefreshTokenStorePort store,
                                     TokenVerifierPort verifier) {
        return new LoginService(loadUserPort, hasher, tokens, store, verifier);
    }

    @Bean
    public VerifyEmailUseCase verifyEmailUseCase(VerificationTokenPort verifierToken,
                                                 LoadUserPort loadUserPort,
                                                 SaveUserPort saveUserPort,
                                                 TokenGeneratorPort tokens,
                                                 RefreshTokenStorePort store,
                                                 TokenVerifierPort verifier) {
        return new VerifyEmailService(verifierToken, loadUserPort, saveUserPort, tokens, store, verifier);
    }

    @Bean
    public RefreshAccessTokenUseCase refreshAccessTokenUseCase(
            TokenVerifierPort tokenVerifier,
            RefreshTokenStorePort store,
            LoadUserPort loadUserPort,
            TokenGeneratorPort tokens
    ) {
        return new RefreshAccessTokenService(tokenVerifier, store, loadUserPort, tokens);
    }

    @Bean
    public LogoutUseCase logoutUseCase(
            TokenVerifierPort tokenVerifier,
            RefreshTokenStorePort store
    ) {
        return new LogoutService(tokenVerifier, store);
    }
}
