package com.aredondocharro.ClothingStore.identity.domain.port.in;

public interface ResetPasswordUseCase {
    void reset(String rawToken, String newPassword);
}