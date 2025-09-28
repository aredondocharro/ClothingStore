package com.aredondocharro.ClothingStore.identity.domain.port.in;


public interface RegisterUserUseCase {
    AuthResult register(String email, String rawPassword);
}
