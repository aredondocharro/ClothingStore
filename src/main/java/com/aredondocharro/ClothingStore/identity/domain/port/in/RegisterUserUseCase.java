package com.aredondocharro.ClothingStore.identity.domain.port.in;


import com.aredondocharro.ClothingStore.identity.domain.model.Email;

public interface RegisterUserUseCase {
    AuthResult register(Email email, String rawPassword, String confirmPassword);
}
