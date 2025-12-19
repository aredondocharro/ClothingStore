package com.aredondocharro.ClothingStore.inventory.application;

import com.aredondocharro.ClothingStore.inventory.contracts.event.StockReserved;
import com.aredondocharro.ClothingStore.inventory.domain.exception.InventoryItemNotFoundException;
import com.aredondocharro.ClothingStore.inventory.domain.exception.StockReservationAlreadyExistsException;
import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItem;
import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItemId;
import com.aredondocharro.ClothingStore.inventory.domain.model.ReservationReference;
import com.aredondocharro.ClothingStore.inventory.domain.model.StockReservation;
import com.aredondocharro.ClothingStore.inventory.domain.model.StockReservationId;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.ReserveStockUseCase;
import com.aredondocharro.ClothingStore.inventory.domain.port.out.InventoryItemRepositoryPort;
import com.aredondocharro.ClothingStore.inventory.domain.port.out.StockReservationRepositoryPort;
import com.aredondocharro.ClothingStore.shared.domain.event.EventBusPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public class ReserveStockService implements ReserveStockUseCase {

    private final InventoryItemRepositoryPort itemRepo;
    private final StockReservationRepositoryPort reservationRepo;
    private final Clock clock;
    private final EventBusPort eventBus;

    @Override
    public StockReservationId reserve(InventoryItemId itemId, ReservationReference reference, int quantity, Instant now) {
        if (itemId == null) throw new IllegalArgumentException("itemId is required");
        if (reference == null) throw new IllegalArgumentException("reference is required");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");

        // 1) Idempotencia por (itemId, reference)
        var existing = reservationRepo.findActiveByItemAndReference(itemId, reference);
        if (existing.isPresent()) {
            if (existing.get().quantity() == quantity) {
                log.info("Reserve idempotent hit itemId={} ref={} qty={} reservationId={}",
                        itemId, reference, quantity, existing.get().id());
                return existing.get().id();
            }
            throw new StockReservationAlreadyExistsException(reference);
        }

        Instant effectiveNow = effectiveNow(now);

        // 2) Cargar item y reservar stock (reglas en el dominio)
        InventoryItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new InventoryItemNotFoundException(itemId));

        InventoryItem updatedItem = item.reserve(quantity, effectiveNow);

        // 3) Crear reserva (agregado separado)
        StockReservation reservation = StockReservation.createNew(
                null,
                itemId,
                reference,
                quantity,
                effectiveNow
        );

        // 4) Persistir (debe ir en transacción -> lo cubriremos con wrapper tx en infraestructura)
        itemRepo.save(updatedItem);
        reservationRepo.save(reservation);

        // 5) Publicar evento
        eventBus.publish(new StockReserved(
                reservation.id().getValue(),
                itemId.getValue(),
                reference.getValue(),
                quantity,
                effectiveNow
        ));

        log.info("Stock reserved itemId={} sku={} ref={} qty={} reservationId={}",
                itemId, updatedItem.sku(), reference, quantity, reservation.id());

        return reservation.id();
    }

    private Instant effectiveNow(Instant now) {
        return now != null ? now : Instant.now(clock);
    }
}
