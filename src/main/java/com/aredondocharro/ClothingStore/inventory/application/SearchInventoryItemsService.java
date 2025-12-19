package com.aredondocharro.ClothingStore.inventory.application;

import com.aredondocharro.ClothingStore.inventory.domain.model.ItemName;
import com.aredondocharro.ClothingStore.inventory.domain.model.Stock;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.*;
import com.aredondocharro.ClothingStore.inventory.domain.port.out.InventoryItemRepositoryPort;
import com.aredondocharro.ClothingStore.inventory.domain.port.out.view.InventoryItemSummaryView;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SearchInventoryItemsService implements SearchInventoryItemsUseCase {

    private final InventoryItemRepositoryPort repo;

    @Override
    public Page<InventoryItemSummary> search(InventorySearchQuery query, PageRequest pageRequest) {
        if (pageRequest == null) throw new IllegalArgumentException("pageRequest is required");

        Page<InventoryItemSummaryView> viewPage = repo.search(query, pageRequest);

        List<InventoryItemSummary> summaries = viewPage.items().stream()
                .map(this::toSummary)
                .toList();

        return new Page<>(summaries, viewPage.total(), viewPage.request());
    }

    private InventoryItemSummary toSummary(InventoryItemSummaryView v) {
        return new InventoryItemSummary(
                v.id(),
                v.sku(),
                ItemName.of(v.name()),
                v.price(),
                new Stock(v.onHand(), v.reserved()),
                v.status()
        );
    }
}
