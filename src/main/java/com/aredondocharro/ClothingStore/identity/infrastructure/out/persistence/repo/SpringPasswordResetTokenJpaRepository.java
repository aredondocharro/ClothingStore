package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo;



import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.*;


import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SpringPasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

    Optional<PasswordResetTokenEntity> findByTokenHashAndExpiresAtAfterAndUsedAtIsNull(
            String tokenHash, Instant now
    );

    void deleteAllByUserId(UUID userId);
}
