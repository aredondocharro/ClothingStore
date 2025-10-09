package com.aredondocharro.ClothingStore.identity.domain.port.in;


import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;

public interface RegisterUserUseCase {
    AuthResult register(IdentityEmail email, String rawPassword, String confirmPassword);
}
