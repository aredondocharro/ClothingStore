package com.aredondocharro.ClothingStore.inventoryTEST.application;

import com.aredondocharro.ClothingStore.inventory.application.ReserveStockService;
import com.aredondocharro.ClothingStore.inventory.domain.exception.InventoryItemNotFoundException;
import com.aredondocharro.ClothingStore.inventory.domain.exception.StockReservationAlreadyExistsException;
import com.aredondocharro.ClothingStore.inventory.domain.model.*;
import com.aredondocharro.ClothingStore.inventory.domain.port.out.InventoryItemRepositoryPort;
import com.aredondocharro.ClothingStore.inventory.domain.port.out.StockReservationRepositoryPort;
import com.aredondocharro.ClothingStore.shared.domain.event.EventBusPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReserveStockServiceTest {

    private InventoryItemRepositoryPort itemRepo;
    private StockReservationRepositoryPort reservationRepo;
    private EventBusPort eventBus;
    private Clock clock;

    private ReserveStockService service;

    private final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");

    @BeforeEach
    void setup() {
        itemRepo = mock(InventoryItemRepositoryPort.class);
        reservationRepo = mock(StockReservationRepositoryPort.class);
        eventBus = mock(EventBusPort.class);
        clock = Clock.fixed(NOW, ZoneOffset.UTC);

        service = new ReserveStockService(itemRepo, reservationRepo, clock, eventBus);
    }

    @Test
    void reserve_whenActiveReservationExistsWithSameQty_returnsSameReservationId_andDoesNothingElse() {
        InventoryItemId itemId = InventoryItemId.newId();
        ReservationReference ref = ReservationReference.of("ORDER-123");

        StockReservation existing = StockReservation.createNew(
                StockReservationId.newId(),
                itemId,
                ref,
                2,
                NOW
        );

        when(reservationRepo.findActiveByItemAndReference(itemId, ref)).thenReturn(Optional.of(existing));

        StockReservationId result = service.reserve(itemId, ref, 2, NOW);

        assertEquals(existing.id(), result);

        verify(itemRepo, never()).findById(any());
        verify(itemRepo, never()).save(any());
        verify(reservationRepo, never()).save(any());
        verify(eventBus, never()).publish(any());
    }

    @Test
    void reserve_whenActiveReservationExistsWithDifferentQty_throws() {
        InventoryItemId itemId = InventoryItemId.newId();
        ReservationReference ref = ReservationReference.of("ORDER-123");

        StockReservation existing = StockReservation.createNew(
                StockReservationId.newId(),
                itemId,
                ref,
                2,
                NOW
        );

        when(reservationRepo.findActiveByItemAndReference(itemId, ref)).thenReturn(Optional.of(existing));

        assertThrows(StockReservationAlreadyExistsException.class, () -> service.reserve(itemId, ref, 3, NOW));

        verify(itemRepo, never()).findById(any());
        verify(itemRepo, never()).save(any());
        verify(reservationRepo, never()).save(any());
        verify(eventBus, never()).publish(any());
    }

    @Test
    void reserve_whenNoExistingReservation_reservesStock_persists_andPublishesEvent() {
        InventoryItemId itemId = InventoryItemId.newId();
        ReservationReference ref = ReservationReference.of("ORDER-999");

        when(reservationRepo.findActiveByItemAndReference(itemId, ref)).thenReturn(Optional.empty());

        InventoryItem item = InventoryItem.createNew(
                itemId,
                Sku.of("TSHIRT-001"),
                ItemName.of("T-Shirt"),
                null,
                InventoryCategory.TOP,
                AccessoryType.NONE,
                Gender.UNISEX,
                Size.M,
                Fabric.COTTON,
                Color.of("Black"),
                Money.eur(new BigDecimal("19.99")),
                10,
                NOW
        );

        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));

        StockReservationId reservationId = service.reserve(itemId, ref, 3, NOW);

        assertNotNull(reservationId);

        verify(itemRepo).save(any(InventoryItem.class));
        verify(reservationRepo).save(any(StockReservation.class));

        verify(eventBus).publish(any());

        // Extra: comprobar que el item guardado tiene reserved=3
        ArgumentCaptor<InventoryItem> itemCaptor = ArgumentCaptor.forClass(InventoryItem.class);
        verify(itemRepo).save(itemCaptor.capture());
        assertEquals(3, itemCaptor.getValue().stock().reserved());
        assertEquals(7, itemCaptor.getValue().stock().available());
    }

    @Test
    void reserve_whenItemNotFound_throws() {
        InventoryItemId itemId = InventoryItemId.newId();
        ReservationReference ref = ReservationReference.of("ORDER-404");

        when(reservationRepo.findActiveByItemAndReference(itemId, ref)).thenReturn(Optional.empty());
        when(itemRepo.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(InventoryItemNotFoundException.class, () -> service.reserve(itemId, ref, 1, NOW));

        verify(itemRepo, never()).save(any());
        verify(reservationRepo, never()).save(any());
        verify(eventBus, never()).publish(any());
    }

    @Test
    void reserve_whenInsufficientStock_throws_andDoesNotPersist() {
        InventoryItemId itemId = InventoryItemId.newId();
        ReservationReference ref = ReservationReference.of("ORDER-LOW");

        when(reservationRepo.findActiveByItemAndReference(itemId, ref)).thenReturn(Optional.empty());

        InventoryItem item = InventoryItem.createNew(
                itemId,
                Sku.of("CAP-001"),
                ItemName.of("Cap"),
                null,
                InventoryCategory.ACCESSORY,
                AccessoryType.HAT,
                Gender.UNISEX,
                Size.ONE_SIZE,
                Fabric.COTTON,
                Color.of("Blue"),
                Money.eur(new BigDecimal("9.99")),
                1,
                NOW
        );

        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(RuntimeException.class, () -> service.reserve(itemId, ref, 2, NOW));

        verify(itemRepo, never()).save(any());
        verify(reservationRepo, never()).save(any());
        verify(eventBus, never()).publish(any());
    }
}
