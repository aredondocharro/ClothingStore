package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo;

import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.VerificationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository
        extends JpaRepository<VerificationTokenEntity, Long> {

    List<VerificationTokenEntity> findByUserIdAndRevokedAtIsNull(UUID userId);

    Optional<VerificationTokenEntity> findByJti(UUID jti);

    boolean existsByJtiAndRevokedAtIsNullAndExpiresAtAfter(UUID jti, Instant now);


    @Modifying
    @Query("""
        update VerificationTokenEntity t
           set t.revokedAt = :revokedAt
         where t.jti = :jti
           and t.revokedAt is null
    """)
    int markAsRevokedByJti(@Param("jti") UUID jti,
                           @Param("revokedAt") Instant revokedAt);
}
