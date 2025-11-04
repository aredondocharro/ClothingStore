// src/main/java/.../identity/infrastructure/config/tx/IdentityTransactionalConfig.java
package com.aredondocharro.ClothingStore.identity.infrastructure.config.tx;

import com.aredondocharro.ClothingStore.identity.domain.port.in.*;
import com.aredondocharro.ClothingStore.identity.infrastructure.tx.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration(proxyBeanMethods = false)
public class IdentityTransactionalConfig {

    @Bean @Primary
    public RegisterUserUseCase registerUserUseCaseTx(@Qualifier("registerUserUseCase") RegisterUserUseCase d) {
        return new TransactionalRegisterUserUseCase(d);
    }
    @Bean @Primary
    public LoginUseCase loginUseCaseTx(@Qualifier("loginUseCase") LoginUseCase d) {
        return new TransactionalLoginUseCase(d);
    }
    @Bean @Primary
    public VerifyEmailUseCase verifyEmailUseCaseTx(@Qualifier("verifyEmailUseCase") VerifyEmailUseCase d) {
        return new TransactionalVerifyEmailUseCase(d);
    }
    @Bean @Primary
    public RefreshAccessTokenUseCase refreshAccessTokenUseCaseTx(@Qualifier("refreshAccessTokenUseCase") RefreshAccessTokenUseCase d) {
        return new TransactionalRefreshAccessTokenUseCase(d);
    }
    @Bean @Primary
    public LogoutUseCase logoutUseCaseTx(@Qualifier("logoutUseCase") LogoutUseCase d) {
        return new TransactionalLogoutUseCase(d);
    }
    @Bean @Primary
    public DeleteUserUseCase deleteUserUseCaseTx(@Qualifier("deleteUserUseCase") DeleteUserUseCase d) {
        return new TransactionalDeleteUserUseCase(d);
    }
    @Bean @Primary
    public UpdateUserRolesUseCase updateUserRolesUseCaseTx(@Qualifier("updateUserRolesUseCase") UpdateUserRolesUseCase d) {
        return new TransactionalUpdateUserRolesUseCase(d);
    }
    @Bean @Primary
    public RequestPasswordResetUseCase requestPasswordResetUseCaseTx(@Qualifier("requestPasswordResetUseCase") RequestPasswordResetUseCase d) {
        return new TransactionalRequestPasswordResetUseCase(d);
    }
    @Bean @Primary
    public ResetPasswordUseCase resetPasswordUseCaseTx(@Qualifier("resetPasswordUseCase") ResetPasswordUseCase d) {
        return new TransactionalResetPasswordUseCase(d);
    }
    @Bean @Primary
    public ChangePasswordUseCase changePasswordUseCaseTx(@Qualifier("changePasswordUseCase") ChangePasswordUseCase d) {
        return new TransactionalChangePasswordUseCase(d);
    }
}
