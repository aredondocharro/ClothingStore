package com.aredondocharro.ClothingStore.inventory.infrastructure.in.web;

import com.aredondocharro.ClothingStore.inventory.domain.model.*;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.GetInventoryItemUseCase;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.InventoryItemSummary;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.SearchInventoryItemsUseCase;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.Page;
import com.aredondocharro.ClothingStore.inventory.infrastructure.in.web.dto.*;
import com.aredondocharro.ClothingStore.inventory.infrastructure.in.web.mapper.InventoryWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inventory/items")
public class InventoryPublicController {

    private final GetInventoryItemUseCase getUseCase;
    private final SearchInventoryItemsUseCase searchUseCase;

    @Operation(summary = "Get inventory item by id")
    @GetMapping("/{id}")
    public ResponseEntity<InventoryItemResponse> getById(@PathVariable String id) {
        InventoryItemId itemId = InventoryItemId.of(id);
        var details = getUseCase.getById(itemId);
        return ResponseEntity.ok(InventoryWebMapper.toResponse(details));
    }

    @Operation(summary = "Search inventory items with filters and pagination")
    @GetMapping
    public ResponseEntity<PagedResponse<InventoryItemSummaryResponse>> search(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) InventoryCategory category,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) Size size,
            @RequestParam(required = false) Fabric fabric,
            @RequestParam(required = false) ItemStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int sizeParam
    ) {
        var query = InventoryWebMapper.toSearchQuery(text, category, gender, size, fabric, status);
        var pageRequest = InventoryWebMapper.toPageRequest(page, sizeParam);

        Page<InventoryItemSummary> result = searchUseCase.search(query, pageRequest);

        @SuppressWarnings("unchecked")
        Page<com.aredondocharro.ClothingStore.inventory.domain.port.in.InventoryItemSummary> typed =
                result;

        List<InventoryItemSummaryResponse> items = typed.items().stream()
                .map(InventoryWebMapper::toSummaryResponse)
                .toList();

        return ResponseEntity.ok(new PagedResponse<>(
                items,
                page,
                pageRequest.limit(),
                typed.total(),
                InventoryWebMapper.totalPages(typed.total(), pageRequest.limit())
        ));
    }
}
