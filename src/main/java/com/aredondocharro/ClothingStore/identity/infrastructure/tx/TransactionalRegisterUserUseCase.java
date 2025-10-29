// TransactionalRegisterUserUseCase.java
package com.aredondocharro.ClothingStore.identity.infrastructure.tx;

import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.in.RegisterUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class TransactionalRegisterUserUseCase implements RegisterUserUseCase {

    private final RegisterUserUseCase delegate;

    @Override
    @Transactional
    public AuthResult register(IdentityEmail email, String rawPassword, String confirmPassword) {
        return delegate.register(email, rawPassword, confirmPassword);
    }
}
