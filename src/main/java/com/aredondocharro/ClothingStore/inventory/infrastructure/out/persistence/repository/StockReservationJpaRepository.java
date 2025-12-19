package com.aredondocharro.ClothingStore.inventory.infrastructure.out.persistence.repository;

import com.aredondocharro.ClothingStore.inventory.domain.model.ReservationStatus;
import com.aredondocharro.ClothingStore.inventory.infrastructure.out.persistence.entity.StockReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StockReservationJpaRepository extends JpaRepository<StockReservationEntity, UUID> {

    Optional<StockReservationEntity> findByItemIdAndReferenceAndStatus(UUID itemId, String reference, ReservationStatus status);
}
