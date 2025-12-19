package com.aredondocharro.ClothingStore.inventory.infrastructure.out.persistence.mapper;

import com.aredondocharro.ClothingStore.inventory.domain.model.*;
import com.aredondocharro.ClothingStore.inventory.infrastructure.out.persistence.entity.InventoryItemEntity;
import com.aredondocharro.ClothingStore.inventory.infrastructure.out.persistence.entity.StockReservationEntity;

import java.util.Currency;

public final class InventoryPersistenceMapper {

    private InventoryPersistenceMapper() {}

    public static InventoryItemEntity toEntity(InventoryItem d) {
        return InventoryItemEntity.builder()
                .id(d.id().getValue())
                .sku(d.sku().getValue())
                .name(d.name().getValue())
                .description(d.description())
                .category(d.category())
                .accessoryType(d.accessoryType())
                .gender(d.gender())
                .size(d.size())
                .fabric(d.fabric())
                .color(d.color() != null ? d.color().getValue() : null)
                .priceAmount(d.price().amount())
                .priceCurrency(d.price().currency().getCurrencyCode())
                .stockOnHand(d.stock().onHand())
                .stockReserved(d.stock().reserved())
                .status(d.status())
                .createdAt(d.createdAt())
                .updatedAt(d.updatedAt())
                .build();
    }

    public static InventoryItem toDomain(InventoryItemEntity e) {
        return new InventoryItem(
                InventoryItemId.of(e.getId()),
                Sku.of(e.getSku()),
                ItemName.of(e.getName()),
                e.getDescription(),
                e.getCategory(),
                e.getAccessoryType(),
                e.getGender(),
                e.getSize(),
                e.getFabric(),
                Color.of(e.getColor()),
                new Money(e.getPriceAmount(), Currency.getInstance(e.getPriceCurrency())),
                new Stock(e.getStockOnHand(), e.getStockReserved()),
                e.getStatus(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public static StockReservationEntity toEntity(StockReservation d) {
        return StockReservationEntity.builder()
                .id(d.id().getValue())
                .itemId(d.itemId().getValue())
                .reference(d.reference().getValue())
                .quantity(d.quantity())
                .status(d.status())
                .createdAt(d.createdAt())
                .releasedAt(d.releasedAt())
                .consumedAt(d.consumedAt())
                .build();
    }

    public static StockReservation toDomain(StockReservationEntity e) {
        return new StockReservation(
                StockReservationId.of(e.getId()),
                InventoryItemId.of(e.getItemId()),
                ReservationReference.of(e.getReference()),
                e.getQuantity(),
                e.getStatus(),
                e.getCreatedAt(),
                e.getReleasedAt(),
                e.getConsumedAt()
        );
    }
}
