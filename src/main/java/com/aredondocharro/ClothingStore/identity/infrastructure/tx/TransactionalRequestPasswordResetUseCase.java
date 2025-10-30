package com.aredondocharro.ClothingStore.identity.infrastructure.tx;

import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.port.in.RequestPasswordResetUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class TransactionalRequestPasswordResetUseCase implements RequestPasswordResetUseCase {

    private final RequestPasswordResetUseCase delegate;

    @Override
    @Transactional
    public void requestReset(IdentityEmail email) {
        delegate.requestReset(email);
    }
}
