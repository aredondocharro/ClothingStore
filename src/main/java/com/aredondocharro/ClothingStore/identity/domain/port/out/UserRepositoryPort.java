package com.aredondocharro.ClothingStore.identity.domain.port.out;

import com.aredondocharro.ClothingStore.identity.domain.port.out.view.CredentialsView;
import com.aredondocharro.ClothingStore.identity.domain.port.out.view.UserView;
import java.util.Optional;
import java.util.UUID;


public interface UserRepositoryPort {
    Optional<CredentialsView> findById(UUID id);
    Optional<CredentialsView> findByEmail(String email);        // sin hash
    void updatePasswordHash(UUID id, String newHash);
    void delete(UUID id);
}

