package com.aredondocharro.ClothingStore.inventoryTEST.domain.model;

import com.aredondocharro.ClothingStore.inventory.domain.exception.ItemDiscontinuedException;
import com.aredondocharro.ClothingStore.inventory.domain.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class InventoryItemTest {

    @Test
    void discontinuedItemCannotBeMutated() {
        Instant now = Instant.parse("2025-01-01T00:00:00Z");

        InventoryItem item = InventoryItem.createNew(
                InventoryItemId.newId(),
                Sku.of("TSHIRT-001"),
                ItemName.of("T-Shirt Basic"),
                "desc",
                InventoryCategory.TOP,
                AccessoryType.NONE,
                Gender.UNISEX,
                Size.M,
                Fabric.COTTON,
                Color.of("Black"),
                new Money(new BigDecimal("19.99"), Currency.getInstance("EUR")),
                10,
                now
        ).discontinue(now.plusSeconds(10));

        assertThrows(ItemDiscontinuedException.class, () -> item.changePrice(Money.eur(new BigDecimal("9.99")), now.plusSeconds(20)));
        assertThrows(ItemDiscontinuedException.class, () -> item.adjustOnHand(5, now.plusSeconds(20)));
        assertThrows(ItemDiscontinuedException.class, () -> item.reserve(1, now.plusSeconds(20)));
    }

    @Test
    void reserveAndReleaseUpdatesStock() {
        Instant now = Instant.parse("2025-01-01T00:00:00Z");

        InventoryItem item = InventoryItem.createNew(
                null,
                Sku.of("HOODIE-XL-RED"),
                ItemName.of("Hoodie"),
                null,
                InventoryCategory.OUTERWEAR,
                AccessoryType.NONE,
                Gender.UNISEX,
                Size.XL,
                Fabric.COTTON,
                Color.of("Red"),
                Money.eur(new BigDecimal("39.90")),
                10,
                now
        );

        InventoryItem reserved = item.reserve(3, now.plusSeconds(5));
        assertEquals(10, reserved.stock().onHand());
        assertEquals(3, reserved.stock().reserved());
        assertEquals(7, reserved.stock().available());

        InventoryItem released = reserved.releaseReserved(2, now.plusSeconds(10));
        assertEquals(1, released.stock().reserved());
        assertEquals(9, released.stock().available());
    }
}
