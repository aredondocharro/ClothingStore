package com.aredondocharro.ClothingStore.identity.infrastructure.tx;

import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.in.LoginUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class TransactionalLoginUseCase implements LoginUseCase {
    private final LoginUseCase delegate;

    @Override
    @Transactional
    public AuthResult login(IdentityEmail email, String rawPassword) {
        return delegate.login(email, rawPassword);
    }
}
