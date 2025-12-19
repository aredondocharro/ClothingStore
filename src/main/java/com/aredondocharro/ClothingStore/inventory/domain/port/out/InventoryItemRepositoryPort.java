package com.aredondocharro.ClothingStore.inventory.domain.port.out;

import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItem;
import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItemId;
import com.aredondocharro.ClothingStore.inventory.domain.model.Sku;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.InventorySearchQuery;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.Page;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.PageRequest;
import com.aredondocharro.ClothingStore.inventory.domain.port.out.view.InventoryItemSummaryView;

import java.util.Optional;

public interface InventoryItemRepositoryPort {
    Optional<InventoryItem> findById(InventoryItemId id);
    Optional<InventoryItem> findBySku(Sku sku);

    void save(InventoryItem item);

    Page<InventoryItemSummaryView> search(InventorySearchQuery query, PageRequest pageRequest);
}
