package com.aredondocharro.ClothingStore.identity.infrastructure.tx;

import com.aredondocharro.ClothingStore.identity.domain.port.in.ResetPasswordUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class TransactionalResetPasswordUseCase implements ResetPasswordUseCase {

    private final ResetPasswordUseCase delegate;

    @Override
    @Transactional
    public void reset(String rawToken, String newPassword) {
        delegate.reset(rawToken, newPassword);
    }
}
