package com.aredondocharro.ClothingStore.identity.domain.port.in;

import com.aredondocharro.ClothingStore.identity.domain.model.UserId;


public interface DeleteUserUseCase {
    void delete(UserId userId);
}
