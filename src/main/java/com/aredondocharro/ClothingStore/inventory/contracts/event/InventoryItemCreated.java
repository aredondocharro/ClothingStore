package com.aredondocharro.ClothingStore.inventory.contracts.event;

import com.aredondocharro.ClothingStore.shared.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record InventoryItemCreated(UUID itemId, String sku, Instant occurredAt) implements DomainEvent {
    @Override public String type() { return "inventory.item.created"; }
}
