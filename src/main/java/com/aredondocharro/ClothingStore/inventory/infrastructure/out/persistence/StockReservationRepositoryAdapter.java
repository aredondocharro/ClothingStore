package com.aredondocharro.ClothingStore.inventory.infrastructure.out.persistence;

import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItemId;
import com.aredondocharro.ClothingStore.inventory.domain.model.ReservationReference;
import com.aredondocharro.ClothingStore.inventory.domain.model.ReservationStatus;
import com.aredondocharro.ClothingStore.inventory.domain.model.StockReservation;
import com.aredondocharro.ClothingStore.inventory.domain.port.out.StockReservationRepositoryPort;
import com.aredondocharro.ClothingStore.inventory.infrastructure.out.persistence.mapper.InventoryPersistenceMapper;
import com.aredondocharro.ClothingStore.inventory.infrastructure.out.persistence.repository.StockReservationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StockReservationRepositoryAdapter implements StockReservationRepositoryPort {

    private final StockReservationJpaRepository jpaRepo;

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<StockReservation> findActiveByItemAndReference(InventoryItemId itemId, ReservationReference reference) {
        if (itemId == null) throw new IllegalArgumentException("itemId is required");
        if (reference == null) throw new IllegalArgumentException("reference is required");

        return jpaRepo.findByItemIdAndReferenceAndStatus(
                        itemId.getValue(),
                        reference.getValue(),
                        ReservationStatus.ACTIVE
                )
                .map(InventoryPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Optional<StockReservation> findByItemAndReferenceAndStatus(
            InventoryItemId itemId,
            ReservationReference reference,
            ReservationStatus status
    ) {
        if (itemId == null) throw new IllegalArgumentException("itemId is required");
        if (reference == null) throw new IllegalArgumentException("reference is required");
        if (status == null) throw new IllegalArgumentException("status is required");

        return jpaRepo.findByItemIdAndReferenceAndStatus(
                        itemId.getValue(),
                        reference.getValue(),
                        status
                )
                .map(InventoryPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void save(StockReservation reservation) {
        if (reservation == null) throw new IllegalArgumentException("reservation is required");
        jpaRepo.save(InventoryPersistenceMapper.toEntity(reservation));
    }
}
