package com.aredondocharro.ClothingStore.inventory.application;

import com.aredondocharro.ClothingStore.inventory.domain.exception.InventoryItemNotFoundException;
import com.aredondocharro.ClothingStore.inventory.domain.exception.StockReservationNotActiveException;
import com.aredondocharro.ClothingStore.inventory.domain.exception.StockReservationNotFoundException;
import com.aredondocharro.ClothingStore.inventory.domain.model.*;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.ConsumeStockUseCase;
import com.aredondocharro.ClothingStore.inventory.domain.port.out.InventoryItemRepositoryPort;
import com.aredondocharro.ClothingStore.inventory.domain.port.out.StockReservationRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public class ConsumeStockService implements ConsumeStockUseCase {

    private final InventoryItemRepositoryPort itemRepo;
    private final StockReservationRepositoryPort reservationRepo;
    private final Clock clock;

    @Override
    public void consume(InventoryItemId itemId, ReservationReference reference, Instant now) {
        if (itemId == null) throw new IllegalArgumentException("itemId is required");
        if (reference == null) throw new IllegalArgumentException("reference is required");

        Instant effectiveNow = (now != null) ? now : Instant.now(clock);

        // 1) Reserva ACTIVE: camino normal
        var active = reservationRepo.findByItemAndReferenceAndStatus(itemId, reference, ReservationStatus.ACTIVE);
        if (active.isPresent()) {
            StockReservation reservation = active.get();

            InventoryItem item = itemRepo.findById(itemId)
                    .orElseThrow(() -> new InventoryItemNotFoundException(itemId));

            InventoryItem updated = item.consumeReserved(reservation.quantity(), effectiveNow);
            StockReservation consumed = reservation.consume(effectiveNow);

            // Debe ir en transacción (Iteración 5.2)
            itemRepo.save(updated);
            reservationRepo.save(consumed);

            log.info("Stock consumed itemId={} sku={} ref={} qty={}",
                    itemId, updated.sku(), reference, reservation.quantity());
            return;
        }

        // 2) Idempotencia: si ya está CONSUMED, no hacemos nada
        var consumed = reservationRepo.findByItemAndReferenceAndStatus(itemId, reference, ReservationStatus.CONSUMED);
        if (consumed.isPresent()) {
            log.info("Consume idempotent hit itemId={} ref={} reservationId={}",
                    itemId, reference, consumed.get().id());
            return;
        }

        // 3) Si está RELEASED, es inconsistencia del flujo (pedido “paid” pero reserva liberada)
        var released = reservationRepo.findByItemAndReferenceAndStatus(itemId, reference, ReservationStatus.RELEASED);
        if (released.isPresent()) {
            throw new StockReservationNotActiveException(reference, ReservationStatus.RELEASED);
        }

        // 4) Si no hay nada, no existe reserva
        throw new StockReservationNotFoundException(reference);
    }
}
