package com.aredondocharro.ClothingStore.identity.domain.port.in;

public interface RefreshAccessTokenUseCase {
    AuthResult refresh(String refreshTokenRaw, String ip, String userAgent);
}
