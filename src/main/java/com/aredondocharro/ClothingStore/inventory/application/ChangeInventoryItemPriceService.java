package com.aredondocharro.ClothingStore.inventory.application;

import com.aredondocharro.ClothingStore.inventory.contracts.event.InventoryItemPriceChanged;
import com.aredondocharro.ClothingStore.inventory.domain.exception.InventoryItemNotFoundException;
import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItem;
import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItemId;
import com.aredondocharro.ClothingStore.inventory.domain.model.Money;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.ChangeInventoryItemPriceUseCase;
import com.aredondocharro.ClothingStore.inventory.domain.port.out.InventoryItemRepositoryPort;
import com.aredondocharro.ClothingStore.shared.domain.event.EventBusPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public class ChangeInventoryItemPriceService implements ChangeInventoryItemPriceUseCase {

    private final InventoryItemRepositoryPort repo;
    private final Clock clock;
    private final EventBusPort eventBus;

    @Override
    public void changePrice(InventoryItemId id, Money newPrice, Instant now) {
        if (id == null) throw new IllegalArgumentException("id is required");
        if (newPrice == null) throw new IllegalArgumentException("newPrice is required");

        InventoryItem item = repo.findById(id)
                .orElseThrow(() -> new InventoryItemNotFoundException(id));

        Instant effectiveNow = effectiveNow(now);

        InventoryItem updated = item.changePrice(newPrice, effectiveNow);
        repo.save(updated);

        eventBus.publish(new InventoryItemPriceChanged(
                updated.id().getValue(),
                updated.price().amount(),
                updated.price().currency().getCurrencyCode(),
                effectiveNow
        ));

        log.info("Inventory item price changed id={} sku={} price={} {}",
                updated.id(), updated.sku(), updated.price().amount(), updated.price().currency().getCurrencyCode());
    }

    private Instant effectiveNow(Instant now) {
        return now != null ? now : Instant.now(clock);
    }
}
