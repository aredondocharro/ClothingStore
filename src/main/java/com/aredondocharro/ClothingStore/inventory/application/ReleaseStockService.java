package com.aredondocharro.ClothingStore.inventory.application;

import com.aredondocharro.ClothingStore.inventory.contracts.event.StockReleased;
import com.aredondocharro.ClothingStore.inventory.domain.exception.InventoryItemNotFoundException;
import com.aredondocharro.ClothingStore.inventory.domain.model.*;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.ReleaseStockUseCase;
import com.aredondocharro.ClothingStore.inventory.domain.port.out.InventoryItemRepositoryPort;
import com.aredondocharro.ClothingStore.inventory.domain.port.out.StockReservationRepositoryPort;
import com.aredondocharro.ClothingStore.shared.domain.event.EventBusPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public class ReleaseStockService implements ReleaseStockUseCase {

    private final InventoryItemRepositoryPort itemRepo;
    private final StockReservationRepositoryPort reservationRepo;
    private final Clock clock;
    private final EventBusPort eventBus;

    @Override
    public void release(InventoryItemId itemId, ReservationReference reference, Instant now) {
        if (itemId == null) throw new IllegalArgumentException("itemId is required");
        if (reference == null) throw new IllegalArgumentException("reference is required");

        Instant effectiveNow = effectiveNow(now);

        var reservationOpt = reservationRepo.findActiveByItemAndReference(itemId, reference);
        if (reservationOpt.isEmpty()) {
            // Idempotente: si ya estaba liberada o nunca existió, no rompemos el flujo.
            log.info("Release ignored (no active reservation) itemId={} ref={}", itemId, reference);
            return;
        }

        StockReservation reservation = reservationOpt.get();

        InventoryItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new InventoryItemNotFoundException(itemId));

        InventoryItem updatedItem = item.releaseReserved(reservation.quantity(), effectiveNow);
        StockReservation releasedReservation = reservation.release(effectiveNow);

        // Debe ejecutarse dentro de una transacción (Iteración 5)
        itemRepo.save(updatedItem);
        reservationRepo.save(releasedReservation);

        eventBus.publish(new StockReleased(
                reservation.id().getValue(),
                itemId.getValue(),
                reference.getValue(),
                reservation.quantity(),
                effectiveNow
        ));

        log.info("Stock released itemId={} sku={} ref={} qty={}", itemId, updatedItem.sku(), reference, reservation.quantity());
    }

    private Instant effectiveNow(Instant now) {
        return now != null ? now : Instant.now(clock);
    }
}
