package com.aredondocharro.ClothingStore.inventory.contracts.event;

import com.aredondocharro.ClothingStore.shared.domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InventoryItemPriceChanged(UUID itemId, BigDecimal amount, String currency, Instant occurredAt) implements DomainEvent {
    @Override public String type() { return "inventory.item.price-changed"; }
}
