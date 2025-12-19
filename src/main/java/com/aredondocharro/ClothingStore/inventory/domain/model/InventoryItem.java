package com.aredondocharro.ClothingStore.inventory.domain.model;

import com.aredondocharro.ClothingStore.inventory.domain.exception.ItemDiscontinuedException;

import java.time.Instant;
import java.util.Objects;

public record InventoryItem(
        InventoryItemId id,
        Sku sku,
        ItemName name,
        String description,
        InventoryCategory category,
        AccessoryType accessoryType,
        Gender gender,
        Size size,
        Fabric fabric,
        Color color,
        Money price,
        Stock stock,
        ItemStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public InventoryItem {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sku, "sku");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(accessoryType, "accessoryType");
        Objects.requireNonNull(gender, "gender");
        Objects.requireNonNull(size, "size");
        Objects.requireNonNull(fabric, "fabric");
        Objects.requireNonNull(price, "price");
        Objects.requireNonNull(stock, "stock");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");

        if (description != null && description.length() > 2000) {
            throw new IllegalArgumentException("Description max length is 2000");
        }
        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("updatedAt cannot be before createdAt");
        }
    }

    public static InventoryItem createNew(
            InventoryItemId id,
            Sku sku,
            ItemName name,
            String description,
            InventoryCategory category,
            AccessoryType accessoryType,
            Gender gender,
            Size size,
            Fabric fabric,
            Color color,
            Money price,
            int initialOnHand,
            Instant now
    ) {
        Objects.requireNonNull(now, "now");
        return new InventoryItem(
                id == null ? InventoryItemId.newId() : id,
                sku,
                name,
                description,
                category,
                accessoryType,
                gender,
                size,
                fabric,
                color,
                price,
                Stock.of(initialOnHand),
                ItemStatus.ACTIVE,
                now,
                now
        );
    }

    private void ensureActive() {
        if (status == ItemStatus.DISCONTINUED) throw new ItemDiscontinuedException();
    }

    public InventoryItem updateDetails(
            ItemName newName,
            String newDescription,
            InventoryCategory newCategory,
            AccessoryType newAccessoryType,
            Gender newGender,
            Size newSize,
            Fabric newFabric,
            Color newColor,
            Instant now
    ) {
        ensureActive();
        Objects.requireNonNull(newName, "newName");
        Objects.requireNonNull(newCategory, "newCategory");
        Objects.requireNonNull(newAccessoryType, "newAccessoryType");
        Objects.requireNonNull(newGender, "newGender");
        Objects.requireNonNull(newSize, "newSize");
        Objects.requireNonNull(newFabric, "newFabric");
        Objects.requireNonNull(now, "now");

        return new InventoryItem(
                id, sku, newName,
                newDescription,
                newCategory,
                newAccessoryType,
                newGender,
                newSize,
                newFabric,
                newColor,
                price,
                stock,
                status,
                createdAt,
                now
        );
    }

    public InventoryItem changePrice(Money newPrice, Instant now) {
        ensureActive();
        Objects.requireNonNull(newPrice, "newPrice");
        Objects.requireNonNull(now, "now");

        if (this.price.equals(newPrice)) return this;

        return new InventoryItem(
                id, sku, name, description, category, accessoryType, gender, size, fabric, color,
                newPrice, stock, status, createdAt, now
        );
    }

    public InventoryItem adjustOnHand(int delta, Instant now) {
        ensureActive();
        Objects.requireNonNull(now, "now");

        Stock newStock = stock.adjustOnHand(delta);
        if (newStock.equals(stock)) return this;

        return new InventoryItem(
                id, sku, name, description, category, accessoryType, gender, size, fabric, color,
                price, newStock, status, createdAt, now
        );
    }

    public InventoryItem reserve(int qty, Instant now) {
        ensureActive();
        Objects.requireNonNull(now, "now");

        Stock newStock = stock.reserve(qty);

        return new InventoryItem(
                id, sku, name, description, category, accessoryType, gender, size, fabric, color,
                price, newStock, status, createdAt, now
        );
    }

    public InventoryItem releaseReserved(int qty, Instant now) {
        // NO ensureActive(): liberar reservas debe ser posible incluso si está DISCONTINUED.
        Objects.requireNonNull(now, "now");

        Stock newStock = stock.release(qty);

        return new InventoryItem(
                id, sku, name, description, category, accessoryType, gender, size, fabric, color,
                price, newStock, status, createdAt, now
        );
    }

    public InventoryItem discontinue(Instant now) {
        Objects.requireNonNull(now, "now");
        if (status == ItemStatus.DISCONTINUED) return this;

        return new InventoryItem(
                id, sku, name, description, category, accessoryType, gender, size, fabric, color,
                price, stock, ItemStatus.DISCONTINUED, createdAt, now
        );
    }

    public InventoryItem reactivate(Instant now) {
        Objects.requireNonNull(now, "now");
        if (status == ItemStatus.ACTIVE) return this;

        return new InventoryItem(
                id, sku, name, description, category, accessoryType, gender, size, fabric, color,
                price, stock, ItemStatus.ACTIVE, createdAt, now
        );
    }
    public InventoryItem consumeReserved(int qty, Instant now) {
        ensureActive(); // consumir stock NO debería ocurrir si está discontinued (venta de un discontinuado)
        java.util.Objects.requireNonNull(now, "now");

        // Importante: primero bajamos reserved (valida qty <= reserved) y luego bajamos onHand
        Stock afterRelease = this.stock.release(qty);
        Stock afterOnHandDecrease = afterRelease.adjustOnHand(-qty);

        return new InventoryItem(
                id, sku, name, description, category, accessoryType, gender, size, fabric, color,
                price, afterOnHandDecrease, status, createdAt, now
        );
    }
}
