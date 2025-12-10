// src/main/java/.../identity/infrastructure/config/usecase/IdentityUseCasesConfig.java
package com.aredondocharro.ClothingStore.identity.infrastructure.config.usecase;

import com.aredondocharro.ClothingStore.identity.application.*;
import com.aredondocharro.ClothingStore.identity.domain.port.in.*;
import com.aredondocharro.ClothingStore.identity.domain.port.out.*;
import com.aredondocharro.ClothingStore.shared.domain.event.EventBusPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;

@Configuration(proxyBeanMethods = false)
public class IdentityUseCasesConfig {

    @Bean
    public RegisterUserUseCase registerUserUseCase(LoadUserPort loadUserPort,
                                                   SaveUserPort saveUserPort,
                                                   PasswordHasherPort hasher,
                                                   PasswordPolicyPort passwordPolicy,
                                                   Clock clock,
                                                   EventBusPort eventBus) {
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
    public VerifyEmailUseCase verifyEmailUseCase(
            VerificationTokenPort verifierToken,
            VerificationTokenStorePort verificationTokenStore,
            LoadUserPort loadUserPort,
            SaveUserPort saveUserPort,
            TokenGeneratorPort tokens,
            RefreshTokenStorePort store,
            RefreshTokenVerifierPort refreshVerifier,
            Clock clock
    ) {
        return new VerifyEmailService(
                verifierToken,
                verificationTokenStore,
                loadUserPort,
                saveUserPort,
                tokens,
                store,
                refreshVerifier,
                clock
        );
    }


    @Bean
    public RefreshAccessTokenUseCase refreshAccessTokenUseCase(
            RefreshTokenVerifierPort refreshVerifier,
            RefreshTokenStorePort store,
            LoadUserPort loadUserPort,
            TokenGeneratorPort tokens) {
        return new RefreshAccessTokenService(refreshVerifier, store, loadUserPort, tokens);
    }

    @Bean
    public LogoutUseCase logoutUseCase(
            RefreshTokenVerifierPort refreshVerifier,
            RefreshTokenStorePort store) {
        return new LogoutService(refreshVerifier, store);
    }

    @Bean
    public DeleteUserUseCase deleteUserUseCase(
            UserAdminRepositoryPort userAdminRepository,
            SessionManagerPort sessionManager) {
        return new DeleteUserService(userAdminRepository, sessionManager);
    }

    @Bean
    public UpdateUserRolesUseCase updateUserRolesUseCase(
            UserAdminRepositoryPort userAdminRepository) {
        return new UpdateUserRolesService(userAdminRepository);
    }

    @Bean
    public RequestPasswordResetUseCase requestPasswordResetUseCase(
            UserRepositoryPort users,
            PasswordResetTokenRepositoryPort tokens,
            EventBusPort eventBus,
            @Value("${app.reset.baseUrl}")
            String baseUrl,
            Clock clock,
            @Value("${app.reset.ttl}") Duration ttl) {
        return new RequestPasswordResetService(users, tokens, eventBus, baseUrl, clock, ttl);
    }

    @Bean
    public ResetPasswordUseCase resetPasswordUseCase(
            PasswordResetTokenRepositoryPort tokens,
            PasswordPolicyPort passwordPolicy,
            UserRepositoryPort users,
            PasswordHasherPort passwordHasher,
            SessionManagerPort sessions,
            Clock clock) {
        return new ResetPasswordService(tokens, passwordPolicy, users, passwordHasher, sessions, clock);
    }

    @Bean
    public ChangePasswordUseCase changePasswordUseCase(
            UserRepositoryPort users,
            PasswordHasherPort passwordHasher,
            PasswordPolicyPort passwordPolicy,
            SessionManagerPort sessions) {
        return new ChangePasswordService(users, passwordHasher, passwordPolicy, sessions);
    }

    @Bean
    public ResendVerificationEmailUseCase resendVerificationEmailUseCase(
            LoadUserPort loadUserPort,
            VerificationTokenRotationPort tokenRotationPort,
            EventBusPort eventBus,
            Clock clock,
            @Value("${app.verify.baseUrl}")
            String verifyBaseUrl) {
        return new ResendVerificationEmailService(loadUserPort, tokenRotationPort, eventBus, clock, verifyBaseUrl);
    }

}
