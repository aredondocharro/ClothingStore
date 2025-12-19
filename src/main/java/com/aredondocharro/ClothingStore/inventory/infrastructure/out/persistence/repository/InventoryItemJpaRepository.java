package com.aredondocharro.ClothingStore.inventory.infrastructure.out.persistence.repository;

import com.aredondocharro.ClothingStore.inventory.infrastructure.out.persistence.entity.InventoryItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InventoryItemJpaRepository extends JpaRepository<InventoryItemEntity, UUID> {
    Optional<InventoryItemEntity> findBySku(String sku);
}
