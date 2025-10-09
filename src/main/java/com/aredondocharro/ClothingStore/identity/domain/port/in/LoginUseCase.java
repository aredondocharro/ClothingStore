package com.aredondocharro.ClothingStore.identity.domain.port.in;

import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;

public interface LoginUseCase {
    AuthResult login(IdentityEmail email, String rawPassword);
}
