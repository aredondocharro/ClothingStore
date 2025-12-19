package com.aredondocharro.ClothingStore.inventory.application;

import com.aredondocharro.ClothingStore.inventory.contracts.event.InventoryItemDiscontinued;
import com.aredondocharro.ClothingStore.inventory.contracts.event.InventoryItemReactivated;
import com.aredondocharro.ClothingStore.inventory.domain.exception.InventoryItemNotFoundException;
import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItem;
import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItemId;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.DiscontinueInventoryItemUseCase;
import com.aredondocharro.ClothingStore.inventory.domain.port.out.InventoryItemRepositoryPort;
import com.aredondocharro.ClothingStore.shared.domain.event.EventBusPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public class DiscontinueInventoryItemService implements DiscontinueInventoryItemUseCase {

    private final InventoryItemRepositoryPort repo;
    private final Clock clock;
    private final EventBusPort eventBus;

    @Override
    public void discontinue(InventoryItemId id, Instant now) {
        if (id == null) throw new IllegalArgumentException("id is required");

        InventoryItem item = repo.findById(id)
                .orElseThrow(() -> new InventoryItemNotFoundException(id));

        Instant effectiveNow = effectiveNow(now);

        InventoryItem updated = item.discontinue(effectiveNow);
        repo.save(updated);

        eventBus.publish(new InventoryItemDiscontinued(updated.id().getValue(), effectiveNow));
        log.info("Inventory item discontinued id={} sku={}", updated.id(), updated.sku());
    }

    @Override
    public void reactivate(InventoryItemId id, Instant now) {
        if (id == null) throw new IllegalArgumentException("id is required");

        InventoryItem item = repo.findById(id)
                .orElseThrow(() -> new InventoryItemNotFoundException(id));

        Instant effectiveNow = effectiveNow(now);

        InventoryItem updated = item.reactivate(effectiveNow);
        repo.save(updated);

        eventBus.publish(new InventoryItemReactivated(updated.id().getValue(), effectiveNow));
        log.info("Inventory item reactivated id={} sku={}", updated.id(), updated.sku());
    }

    private Instant effectiveNow(Instant now) {
        return now != null ? now : Instant.now(clock);
    }
}
