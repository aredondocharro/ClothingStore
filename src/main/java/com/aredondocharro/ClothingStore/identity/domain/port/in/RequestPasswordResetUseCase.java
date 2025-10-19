package com.aredondocharro.ClothingStore.identity.domain.port.in;

import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;


public interface RequestPasswordResetUseCase {
    void requestReset(IdentityEmail email);
}