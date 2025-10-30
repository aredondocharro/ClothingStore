package com.aredondocharro.ClothingStore.identity.infrastructure.tx;

import com.aredondocharro.ClothingStore.identity.domain.port.in.LogoutUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class TransactionalLogoutUseCase implements LogoutUseCase {
    private final LogoutUseCase delegate;

    @Override
    @Transactional
    public void logout(String refreshTokenRaw, String ip) {
        delegate.logout(refreshTokenRaw, ip);
    }
}
