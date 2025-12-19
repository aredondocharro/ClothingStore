package com.aredondocharro.ClothingStore.inventory.application;

import com.aredondocharro.ClothingStore.inventory.contracts.event.InventoryStockAdjusted;
import com.aredondocharro.ClothingStore.inventory.domain.exception.InventoryItemNotFoundException;
import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItem;
import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItemId;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.AdjustInventoryStockUseCase;
import com.aredondocharro.ClothingStore.inventory.domain.port.out.InventoryItemRepositoryPort;
import com.aredondocharro.ClothingStore.shared.domain.event.EventBusPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public class AdjustInventoryStockService implements AdjustInventoryStockUseCase {

    private final InventoryItemRepositoryPort repo;
    private final Clock clock;
    private final EventBusPort eventBus;

    @Override
    public void adjustOnHand(InventoryItemId id, int delta, String reason, Instant now) {
        if (id == null) throw new IllegalArgumentException("id is required");
        if (reason == null || reason.isBlank()) reason = "admin-adjustment";

        InventoryItem item = repo.findById(id)
                .orElseThrow(() -> new InventoryItemNotFoundException(id));

        Instant effectiveNow = effectiveNow(now);

        InventoryItem updated = item.adjustOnHand(delta, effectiveNow);
        repo.save(updated);

        eventBus.publish(new InventoryStockAdjusted(updated.id().getValue(), delta, reason, effectiveNow));
        log.info("Inventory stock adjusted id={} sku={} delta={} reason={}", updated.id(), updated.sku(), delta, reason);
    }

    private Instant effectiveNow(Instant now) {
        return now != null ? now : Instant.now(clock);
    }
}
