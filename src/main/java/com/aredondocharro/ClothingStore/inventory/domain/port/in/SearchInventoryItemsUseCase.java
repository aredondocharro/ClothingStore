package com.aredondocharro.ClothingStore.inventory.domain.port.in;

public interface SearchInventoryItemsUseCase {
    Page<InventoryItemSummary> search(InventorySearchQuery query, PageRequest pageRequest);
}
