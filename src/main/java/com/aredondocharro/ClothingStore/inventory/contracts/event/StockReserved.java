package com.aredondocharro.ClothingStore.inventory.contracts.event;

import com.aredondocharro.ClothingStore.shared.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record StockReserved(UUID reservationId, UUID itemId, String reference, int quantity, Instant occurredAt) implements DomainEvent {
    @Override public String type() { return "inventory.stock.reserved"; }
}
