package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo;

import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.RefreshSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataRefreshSessionRepository extends JpaRepository<RefreshSessionEntity, String> {
    Optional<RefreshSessionEntity> findByJti(String jti);
}
