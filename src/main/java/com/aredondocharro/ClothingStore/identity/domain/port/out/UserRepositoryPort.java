package com.aredondocharro.ClothingStore.identity.domain.port.out;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    Optional<UserView> findByEmail(String email);

    Optional<UserView> findById(UUID id);

    void updatePasswordHash(UUID id, String newHash);

    final class UserView {
        private final UUID id;
        private final String email;
        private final String passwordHash;
        private final boolean emailVerified;

        public UserView(UUID id, String email, String passwordHash, boolean emailVerified) {
            this.id = id;
            this.email = email;
            this.passwordHash = passwordHash;
            this.emailVerified = emailVerified;
        }

        public UUID id() { return id; }
        public String email() { return email; }
        public String passwordHash() { return passwordHash; }
        public boolean emailVerified() { return emailVerified; }
    }
}
