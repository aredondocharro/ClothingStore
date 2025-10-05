package com.aredondocharro.ClothingStore.identity.domain.port.in;

public interface RequestPasswordResetUseCase {
    void requestReset(String email);
}