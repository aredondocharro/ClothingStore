package com.aredondocharro.ClothingStore.identity.infrastructure.config;

import com.aredondocharro.ClothingStore.identity.application.*;
import com.aredondocharro.ClothingStore.identity.domain.port.in.*;
import com.aredondocharro.ClothingStore.identity.domain.port.out.*;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.crypto.BCryptPasswordHasherAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt.JwtRefreshTokenVerifierAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt.JwtTokenGeneratorAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.jwt.JwtVerificationAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.JpaRefreshTokenStoreAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.UserAdminRepositoryAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.UserPersistenceAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.UserRepositoryAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.PasswordResetTokenRepositoryAdapter;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.mapper.PasswordResetTokenMapper;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.mapper.RefreshSessionEntityMapper;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.mapper.UserMapper;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringDataRefreshSessionRepository;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringDataUserRepository;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo.SpringPasswordResetTokenJpaRepository;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.policy.NoopSessionManager;
import com.aredondocharro.ClothingStore.identity.infrastructure.out.policy.SimplePasswordPolicy;
import com.aredondocharro.ClothingStore.identity.infrastructure.tx.*;
import com.aredondocharro.ClothingStore.shared.domain.event.EventBusPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Duration;


@Configuration
public class IdentityConfig {

    // ========================================================================
    // Adapters (OUT)
    // ========================================================================

    /**
     * JPA adapter implementing LoadUserPort / SaveUserPort (domain User)
     */
    @Bean
    public UserPersistenceAdapter userPersistenceAdapter(SpringDataUserRepository repo,
                                                         UserMapper mapper) {
        return new UserPersistenceAdapter(repo, mapper);
    }

    /**
     * Adapter for UserRepositoryPort (CredentialsView + updatePasswordHash)
     */
    @Bean
    public UserRepositoryPort userRepositoryPort(SpringDataUserRepository repo,
                                                 UserMapper mapper) {
        return new UserRepositoryAdapter(repo, mapper);
    }

    /**
     * Adapter para PasswordResetTokenRepositoryPort (tokens de reset)
     */
    @Bean
    public PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort(
            SpringPasswordResetTokenJpaRepository jpa,
            PasswordResetTokenMapper mapper
    ) {
        return new PasswordResetTokenRepositoryAdapter(jpa, mapper);
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
    public RefreshTokenStorePort refreshTokenStorePort(SpringDataRefreshSessionRepository repo,
                                                       RefreshSessionEntityMapper mapper) {
        return new JpaRefreshTokenStoreAdapter(repo, mapper);
    }

    // ========================================================================
    // Seguridad / JWT (Ports OUT)
    // ========================================================================

    @Bean
    public RefreshTokenVerifierPort refreshTokenVerifierPort(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.issuer}") String issuer
    ) {
        return new JwtRefreshTokenVerifierAdapter(secret, issuer);
    }

    @Bean
    public VerificationTokenPort verificationTokenPort(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.issuer}") String issuer
    ) {
        return new JwtVerificationAdapter(secret, issuer);
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
                                     RefreshTokenVerifierPort refreshVerifier,
                                     Clock clock) {
        return new LoginService(loadUserPort, hasher, tokens, store, refreshVerifier, clock);
    }

        @Bean
        public VerifyEmailUseCase verifyEmailUseCase(VerificationTokenPort verifierToken,
                LoadUserPort loadUserPort,
                SaveUserPort saveUserPort,
                TokenGeneratorPort tokens,
                RefreshTokenStorePort store,
                RefreshTokenVerifierPort refreshVerifier,
                Clock clock) {
            return new VerifyEmailService(verifierToken, loadUserPort, saveUserPort, tokens, store, refreshVerifier, clock);
        }



    @Bean
    public RefreshAccessTokenUseCase refreshAccessTokenUseCase(
            RefreshTokenVerifierPort refreshVerifier,
            RefreshTokenStorePort store,
            LoadUserPort loadUserPort,
            TokenGeneratorPort tokens
    ) {
        return new RefreshAccessTokenService(refreshVerifier, store, loadUserPort, tokens);
    }


    @Bean
    public LogoutUseCase logoutUseCase(
            RefreshTokenVerifierPort refreshVerifier,
            RefreshTokenStorePort store
    ) {
        return new LogoutService(refreshVerifier, store);
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

    // ========================================================================
    // Publish events services
    // ========================================================================


    @Bean
    PublishVerificationEmailOnUserRegisteredService publishVerificationEmailOnUserRegisteredService(
            TokenGeneratorPort tokens,
            LoadUserPort loadUsers,
            EventBusPort eventBus,
            Clock clock,
            @Value("${app.verify.baseUrl}") String verifyBaseUrl
    ) {
        return new PublishVerificationEmailOnUserRegisteredService(tokens, loadUsers, eventBus, clock, verifyBaseUrl);
    }

    // ========================================================================
    // TX Management
    // ========================================================================

    //Por qué @Primary: Ya existe un bean del mismo tipo (el servicio puro).
    // Con @Primary, cuando un controlador pide RegisterUserUseCase, Spring entrega el wrapper transaccional.

    @Bean
    @Primary
    public RegisterUserUseCase registerUserUseCaseTx(@Qualifier("registerUserUseCase") RegisterUserUseCase delegate) {
        return new TransactionalRegisterUserUseCase(delegate);
    }

    @Bean
    @Primary
    public LoginUseCase loginUseCaseTx(@Qualifier("loginUseCase") LoginUseCase delegate) {
        return new TransactionalLoginUseCase(delegate);
    }

    @Bean
    @Primary
    public VerifyEmailUseCase verifyEmailUseCaseTx(@Qualifier("verifyEmailUseCase") VerifyEmailUseCase delegate) {
        return new TransactionalVerifyEmailUseCase(delegate);
    }

    @Bean
    @Primary
    public RefreshAccessTokenUseCase refreshAccessTokenUseCaseTx(@Qualifier("refreshAccessTokenUseCase") RefreshAccessTokenUseCase delegate) {
        return new TransactionalRefreshAccessTokenUseCase(delegate);
    }

    @Bean
    @Primary
    public LogoutUseCase logoutUseCaseTx(@Qualifier("logoutUseCase") LogoutUseCase delegate) {
        return new TransactionalLogoutUseCase(delegate);
    }

    @Bean
    @Primary
    public DeleteUserUseCase deleteUserUseCaseTx(@Qualifier("deleteUserUseCase") DeleteUserUseCase delegate) {
        return new TransactionalDeleteUserUseCase(delegate);
    }

    @Bean
    @Primary
    public UpdateUserRolesUseCase updateUserRolesUseCaseTx(@Qualifier("updateUserRolesUseCase") UpdateUserRolesUseCase delegate) {
        return new TransactionalUpdateUserRolesUseCase(delegate);
    }

    @Bean
    @Primary
    public RequestPasswordResetUseCase requestPasswordResetUseCaseTx(@Qualifier("requestPasswordResetUseCase") RequestPasswordResetUseCase delegate) {
        return new TransactionalRequestPasswordResetUseCase(delegate);
    }

    @Bean
    @Primary
    public ResetPasswordUseCase resetPasswordUseCaseTx(@Qualifier("resetPasswordUseCase") ResetPasswordUseCase delegate) {
        return new TransactionalResetPasswordUseCase(delegate);
    }

    @Bean
    @Primary
    public ChangePasswordUseCase changePasswordUseCaseTx(@Qualifier("changePasswordUseCase") ChangePasswordUseCase delegate) {
        return new TransactionalChangePasswordUseCase(delegate);
    }

}
