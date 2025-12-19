package com.aredondocharro.ClothingStore.inventory.application;

import com.aredondocharro.ClothingStore.inventory.contracts.event.InventoryItemCreated;
import com.aredondocharro.ClothingStore.inventory.domain.exception.SkuAlreadyExistsException;
import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItem;
import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItemId;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.CreateInventoryItemCommand;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.CreateInventoryItemUseCase;
import com.aredondocharro.ClothingStore.inventory.domain.port.out.InventoryItemRepositoryPort;
import com.aredondocharro.ClothingStore.shared.domain.event.EventBusPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public class CreateInventoryItemService implements CreateInventoryItemUseCase {

    private final InventoryItemRepositoryPort repo;
    private final Clock clock;
    private final EventBusPort eventBus;

    @Override
    public InventoryItemId create(CreateInventoryItemCommand command) {
        if (command == null) throw new IllegalArgumentException("command is required");

        repo.findBySku(command.sku()).ifPresent(existing -> {
            throw new SkuAlreadyExistsException(command.sku());
        });

        Instant now = effectiveNow(command.now());

        InventoryItem item = InventoryItem.createNew(
                null,
                command.sku(),
                command.name(),
                command.description(),
                command.category(),
                command.accessoryType(),
                command.gender(),
                command.size(),
                command.fabric(),
                command.color(),
                command.price(),
                command.initialOnHand(),
                now
        );

        repo.save(item);

        eventBus.publish(new InventoryItemCreated(item.id().getValue(), item.sku().getValue(), now));
        log.info("Inventory item created id={} sku={}", item.id(), item.sku());

        return item.id();
    }

    private Instant effectiveNow(Instant now) {
        return now != null ? now : Instant.now(clock);
    }
}
