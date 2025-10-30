package com.aredondocharro.ClothingStore.identity.infrastructure.tx;

import com.aredondocharro.ClothingStore.identity.domain.port.in.AuthResult;
import com.aredondocharro.ClothingStore.identity.domain.port.in.RefreshAccessTokenUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class TransactionalRefreshAccessTokenUseCase implements RefreshAccessTokenUseCase {
    private final RefreshAccessTokenUseCase delegate;

    @Override
    @Transactional
    public AuthResult refresh(String refreshTokenRaw, String ip, String userAgent) {
        return delegate.refresh(refreshTokenRaw, ip, userAgent);
    }
}
