package com.aredondocharro.ClothingStore.identity.domain.port.in;

public interface LogoutUseCase {
    void logout(String refreshTokenRaw, String ip);
}
