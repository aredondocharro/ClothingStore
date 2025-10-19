package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo;

import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.RefreshSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataRefreshSessionRepository extends JpaRepository<RefreshSessionEntity, String> {
    Optional<RefreshSessionEntity> findByJti(String jti);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                update RefreshSessionEntity rs
                set rs.revokedAt = :when
                where rs.userId = :userId and rs.revokedAt is null
            """)
    int revokeAllForUser(@Param("userId") UUID userId, @Param("when") Instant when);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                update RefreshSessionEntity rs
                set rs.revokedAt = :when, rs.replacedByJti = :newJti
                where rs.jti = :oldJti and rs.revokedAt is null
            """)
    int markReplaced(@Param("oldJti") String oldJti,
                     @Param("newJti") String newJti,
                     @Param("when") Instant when);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                update RefreshSessionEntity rs
                set rs.revokedAt = :when
                where rs.jti = :jti and rs.revokedAt is null
            """)
    int revoke(@Param("jti") String jti, @Param("when") Instant when);

    List<RefreshSessionEntity> findByUserIdAndRevokedAtIsNull(UUID userId);
}


