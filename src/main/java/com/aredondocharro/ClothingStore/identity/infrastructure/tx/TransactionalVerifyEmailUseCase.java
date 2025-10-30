package com.aredondocharro.ClothingStore.identity.infrastructure.tx;

import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.in.VerifyEmailUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class TransactionalVerifyEmailUseCase implements VerifyEmailUseCase {
    private final VerifyEmailUseCase delegate;

    @Override
    @Transactional
    public AuthResult verify(String verificationToken) {
        return delegate.verify(verificationToken);
    }
}
