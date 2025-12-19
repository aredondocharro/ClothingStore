package com.aredondocharro.ClothingStore.inventory.infrastructure.in.web.mapper;

import com.aredondocharro.ClothingStore.inventory.domain.model.*;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.*;
import com.aredondocharro.ClothingStore.inventory.infrastructure.in.web.dto.*;

import java.math.BigDecimal;

public final class InventoryWebMapper {

    private InventoryWebMapper() {}

    public static CreateInventoryItemCommand toCreateCommand(CreateInventoryItemRequest r) {
        return new CreateInventoryItemCommand(
                Sku.of(r.sku()),
                ItemName.of(r.name()),
                r.description(),
                r.category(),
                r.accessoryType(),
                r.gender(),
                r.size(),
                r.fabric(),
                Color.of(r.color()),
                Money.of(r.priceAmount(), r.priceCurrency()),
                r.initialOnHand(),
                null // now -> lo pone el servicio vía Clock
        );
    }

    public static UpdateInventoryItemCommand toUpdateCommand(InventoryItemId id, UpdateInventoryItemRequest r) {
        return new UpdateInventoryItemCommand(
                id,
                ItemName.of(r.name()),
                r.description(),
                r.category(),
                r.accessoryType(),
                r.gender(),
                r.size(),
                r.fabric(),
                Color.of(r.color()),
                null // now -> Clock
        );
    }

    public static InventoryItemResponse toResponse(InventoryItemDetails d) {
        return new InventoryItemResponse(
                d.id().getValue(),
                d.sku().getValue(),
                d.name().getValue(),
                d.description(),
                d.category(),
                d.accessoryType(),
                d.gender(),
                d.size(),
                d.fabric(),
                d.color() != null ? d.color().getValue() : null,
                d.price().amount(),
                d.price().currency().getCurrencyCode(),
                d.stock().onHand(),
                d.stock().reserved(),
                d.stock().available(),
                d.status(),
                d.createdAt(),
                d.updatedAt()
        );
    }

    public static InventoryItemSummaryResponse toSummaryResponse(InventoryItemSummary s) {
        return new InventoryItemSummaryResponse(
                s.id().getValue(),
                s.sku().getValue(),
                s.name().getValue(),
                s.price().amount(),
                s.price().currency().getCurrencyCode(),
                s.stock().onHand(),
                s.stock().reserved(),
                s.stock().available(),
                s.status()
        );
    }

    public static InventorySearchQuery toSearchQuery(
            String text,
            InventoryCategory category,
            Gender gender,
            Size size,
            Fabric fabric,
            ItemStatus status
    ) {
        return new InventorySearchQuery(text, category, gender, size, fabric, status);
    }

    public static PageRequest toPageRequest(int page, int size) {
        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size <= 0) throw new IllegalArgumentException("size must be > 0");

        int limit = Math.min(size, 200);
        int offset = Math.multiplyExact(page, limit); // overflow-safe
        return PageRequest.of(offset, limit);
    }

    public static int totalPages(long totalItems, int size) {
        if (size <= 0) return 0;
        return (int) Math.ceil((double) totalItems / (double) size);
    }
}
