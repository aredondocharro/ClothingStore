package com.aredondocharro.ClothingStore.inventory.application;

import com.aredondocharro.ClothingStore.inventory.domain.exception.InventoryItemNotFoundException;
import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItem;
import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItemId;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.GetInventoryItemUseCase;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.InventoryItemDetails;
import com.aredondocharro.ClothingStore.inventory.domain.port.out.InventoryItemRepositoryPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetInventoryItemService implements GetInventoryItemUseCase {

    private final InventoryItemRepositoryPort repo;

    @Override
    public InventoryItemDetails getById(InventoryItemId id) {
        if (id == null) throw new IllegalArgumentException("id is required");

        InventoryItem item = repo.findById(id)
                .orElseThrow(() -> new InventoryItemNotFoundException(id));

        return new InventoryItemDetails(
                item.id(),
                item.sku(),
                item.name(),
                item.description(),
                item.category(),
                item.accessoryType(),
                item.gender(),
                item.size(),
                item.fabric(),
                item.color(),
                item.price(),
                item.stock(),
                item.status(),
                item.createdAt(),
                item.updatedAt()
        );
    }
}
