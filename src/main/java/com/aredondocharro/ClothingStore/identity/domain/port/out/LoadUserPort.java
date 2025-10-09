package com.aredondocharro.ClothingStore.identity.domain.port.out;

import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface LoadUserPort {
    Optional<User> findByEmail(IdentityEmail email);
    Optional<User> findById(UUID id);
}

