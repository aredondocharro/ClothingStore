package com.aredondocharro.ClothingStore.identity.domain.port.in;

public interface LoginUseCase {
    AuthResult login(String email, String rawPassword);
}
