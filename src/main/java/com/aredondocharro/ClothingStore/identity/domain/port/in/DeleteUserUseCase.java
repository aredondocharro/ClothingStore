package com.aredondocharro.ClothingStore.identity.domain.port.in;

import java.util.UUID;

public interface DeleteUserUseCase {
    void delete(UUID userId);
}
