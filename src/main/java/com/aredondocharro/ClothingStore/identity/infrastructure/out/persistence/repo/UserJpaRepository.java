package com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.repo;

import com.aredondocharro.ClothingStore.identity.infrastructure.out.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmailIgnoreCase(String email);
}
