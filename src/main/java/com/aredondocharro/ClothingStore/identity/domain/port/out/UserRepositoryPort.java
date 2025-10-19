package com.aredondocharro.ClothingStore.identity.domain.port.out;

import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.identity.domain.port.out.view.CredentialsView;

import java.util.Optional;


public interface UserRepositoryPort {
    Optional<CredentialsView> findById(UserId id);
    Optional<CredentialsView> findByEmail(IdentityEmail email);        // sin hash
    void updatePasswordHash(UserId id, String newHash);
    void delete(UserId id);
}

