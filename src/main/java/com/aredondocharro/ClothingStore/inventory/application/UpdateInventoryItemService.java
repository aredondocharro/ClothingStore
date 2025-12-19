package com.aredondocharro.ClothingStore.inventory.application;

import com.aredondocharro.ClothingStore.inventory.domain.exception.InventoryItemNotFoundException;
import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItem;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.UpdateInventoryItemCommand;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.UpdateInventoryItemUseCase;
import com.aredondocharro.ClothingStore.inventory.domain.port.out.InventoryItemRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public class UpdateInventoryItemService implements UpdateInventoryItemUseCase {

    private final InventoryItemRepositoryPort repo;
    private final Clock clock;

    @Override
    public void update(UpdateInventoryItemCommand command) {
        if (command == null) throw new IllegalArgumentException("command is required");

        InventoryItem item = repo.findById(command.id())
                .orElseThrow(() -> new InventoryItemNotFoundException(command.id()));

        Instant now = effectiveNow(command.now());

        InventoryItem updated = item.updateDetails(
                command.name(),
                command.description(),
                command.category(),
                command.accessoryType(),
                command.gender(),
                command.size(),
                command.fabric(),
                command.color(),
                now
        );

        repo.save(updated);
        log.info("Inventory item updated id={} sku={}", updated.id(), updated.sku());
    }

    private Instant effectiveNow(Instant now) {
        return now != null ? now : Instant.now(clock);
    }
}
