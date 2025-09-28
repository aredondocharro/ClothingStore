package com.aredondocharro.ClothingStore.identity.adapter.out.persistence.repo;

import com.aredondocharro.ClothingStore.identity.adapter.out.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
    // findById(UUID id) ya lo da JpaRepository
}
