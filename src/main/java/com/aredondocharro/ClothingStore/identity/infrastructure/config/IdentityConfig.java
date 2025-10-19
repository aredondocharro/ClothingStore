package com.aredondocharro.ClothingStore.identity.infrastructure.config;

import com.aredondocharro.ClothingStore.identity.application.*;
import com.aredondocharro.ClothingStore.identity.domain.port.in.*;
import com.aredondocharro.ClothingStore.identity.domain.port.out.*;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.crypto.BCryptPasswordHasherAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt.JwtTokenGeneratorAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt.JwtTokenVerifierAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt.JwtVerificationAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.JpaRefreshTokenStoreAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.UserAdminRepositoryAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.UserPersistenceAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.UserRepositoryAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.PasswordResetTokenRepositoryAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringDataRefreshSessionRepository;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringDataUserRepository;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringPasswordResetTokenJpaRepository;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.security.NoopSessionManager;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.security.SimplePasswordPolicy;
import com.aredondocharro.ClothingStore.shared.domain.event.EventBusPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;


@Configuration
public class IdentityConfig {

    // ========================================================================
    // Adapters (OUT)
    // ========================================================================

    /**
     * Adapter JPA que implementa LoadUserPort / SaveUserPort (dominio User)
     */
    @Bean
    public UserPersistenceAdapter userPersistenceAdapter(SpringDataUserRepository repo) {
        return new UserPersistenceAdapter(repo);
    }

    /**
     * Adapter para UserRepositoryPort (UserView + updatePasswordHash)
     */
    @Bean
    public UserRepositoryPort userRepositoryPort(SpringDataUserRepository repo) {
        return new UserRepositoryAdapter(repo);
    }

    /**
     * Adapter para PasswordResetTokenRepositoryPort (tokens de reset)
     */
    @Bean
    public PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort(SpringPasswordResetTokenJpaRepository jpa) {
        return new PasswordResetTokenRepositoryAdapter(jpa);
    }

    /**
     * Hasher de contraseñas (BCrypt)
     */
    @Bean
    public PasswordHasherPort passwordHasherPort() {
        return new BCryptPasswordHasherAdapter();
    }

    /**
     * Persistencia de sesiones/refresh tokens (JPA)
     */
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
    RequestPasswordResetUseCase requestPasswordResetUseCase(
            UserRepositoryPort users,
            PasswordResetTokenRepositoryPort tokens,
            EventBusPort eventBus,
            @Value("${app.reset.baseUrl}") String baseUrl,
            Clock clock,
            @Value("${app.reset.ttl}") Duration ttl
    ) {
        return new RequestPasswordResetService(users, tokens, eventBus, baseUrl, clock, ttl);
    }

    @Bean
    ResetPasswordUseCase resetPasswordUseCase(
            PasswordResetTokenRepositoryPort tokens,
            PasswordPolicyPort passwordPolicy,
            UserRepositoryPort users,
            PasswordHasherPort passwordHasher,
            SessionManagerPort sessions,
            Clock clock
    ) {
        return new ResetPasswordService(tokens, passwordPolicy, users, passwordHasher, sessions, clock);
    }

    @Bean
    ChangePasswordUseCase changePasswordUseCase(
            UserRepositoryPort users,
            PasswordHasherPort passwordHasher,
            PasswordPolicyPort passwordPolicy,
            SessionManagerPort sessions) {
        return new ChangePasswordService(users, passwordHasher, passwordPolicy, sessions);
    }
// ========================================================================
    // Admin user management (Port OUT)
    // ========================================================================
    @Bean
    public UserAdminRepositoryPort userAdminRepositoryPort(SpringDataUserRepository repo) {
        return new UserAdminRepositoryAdapter(repo);
    }



    // ========================================================================
    // Use cases (IN)
    // ========================================================================

    // dentro de IdentityConfig
    @Bean
    public RegisterUserUseCase registerUserUseCase(
            LoadUserPort loadUserPort,
            SaveUserPort saveUserPort,
            PasswordHasherPort hasher,
            PasswordPolicyPort passwordPolicy,
            Clock clock,
            EventBusPort eventBus
    ) {
        return new RegisterUserService(loadUserPort, saveUserPort, hasher, passwordPolicy, clock, eventBus);
    }


    @Bean
    public LoginUseCase loginUseCase(LoadUserPort loadUserPort,
                                     PasswordHasherPort hasher,
                                     TokenGeneratorPort tokens,
                                     RefreshTokenStorePort store,
                                     TokenVerifierPort verifier,
                                     Clock clock) {
        return new LoginService(loadUserPort, hasher, tokens, store, verifier, clock);
    }

    @Bean
    public VerifyEmailUseCase verifyEmailUseCase(VerificationTokenPort verifierToken,
                                                 LoadUserPort loadUserPort,
                                                 SaveUserPort saveUserPort,
                                                 TokenGeneratorPort tokens,
                                                 RefreshTokenStorePort store,
                                                 TokenVerifierPort verifier,
                                                 Clock clock) {
        return new VerifyEmailService(verifierToken, loadUserPort, saveUserPort, tokens, store, verifier, clock);
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

    //Added for admin user deletion
    @Bean
    public DeleteUserUseCase deleteUserUseCase(
            UserAdminRepositoryPort userAdminRepository,
            SessionManagerPort sessionManager
    ) {
        return new DeleteUserService(userAdminRepository, sessionManager);
    }

    @Bean
    public UpdateUserRolesUseCase updateUserRolesUseCase(
            UserAdminRepositoryPort userAdminRepository
    ) {
        return new UpdateUserRolesService(userAdminRepository);
    }
}
