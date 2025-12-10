package com.aredondocharro.ClothingStore.identity.infrastructure.tx;

import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import org.springframework.transaction.annotation.Transactional;
import com.aredondocharro.ClothingStore.identity.domain.port.in.ResendVerificationEmailUseCase;

public class TransactionalResendVerificationEmailUseCase implements ResendVerificationEmailUseCase {

    private final ResendVerificationEmailUseCase delegate;

    @Transactional
    @Override
    public void resend(IdentityEmail email) {
        delegate.resend(email);
    }

    public TransactionalResendVerificationEmailUseCase(ResendVerificationEmailUseCase delegate) {
        this.delegate = delegate;
    }
}
