package com.aredondocharro.ClothingStore.identity.domain.port.in;

import com.aredondocharro.ClothingStore.identity.domain.model.Email;

public interface LoginUseCase {
    AuthResult login(Email email, String rawPassword);
}
