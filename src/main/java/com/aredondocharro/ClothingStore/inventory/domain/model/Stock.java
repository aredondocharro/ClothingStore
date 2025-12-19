package com.aredondocharro.ClothingStore.inventory.domain.model;

import com.aredondocharro.ClothingStore.inventory.domain.exception.InsufficientStockException;
import com.aredondocharro.ClothingStore.inventory.domain.exception.InvalidStockException;

public record Stock(int onHand, int reserved) {

    public Stock {
        if (onHand < 0) throw new InvalidStockException("onHand must be >= 0");
        if (reserved < 0) throw new InvalidStockException("reserved must be >= 0");
        if (reserved > onHand) throw new InvalidStockException("reserved must be <= onHand");
    }

    public static Stock of(int onHand) {
        return new Stock(onHand, 0);
    }

    public int available() {
        return onHand - reserved;
    }

    public Stock reserve(int qty) {
        if (qty <= 0) throw new InvalidStockException("Reservation qty must be > 0");
        if (qty > available()) {
            throw new InsufficientStockException("Not enough available stock to reserve. available=" + available() + ", qty=" + qty);
        }
        return new Stock(onHand, reserved + qty);
    }

    public Stock release(int qty) {
        if (qty <= 0) throw new InvalidStockException("Release qty must be > 0");
        if (qty > reserved) throw new InvalidStockException("Cannot release more than reserved. reserved=" + reserved + ", qty=" + qty);
        return new Stock(onHand, reserved - qty);
    }

    public Stock adjustOnHand(int delta) {
        long newOnHand = (long) onHand + delta;
        if (newOnHand < 0) throw new InvalidStockException("onHand cannot be negative after adjustment");
        if (newOnHand < reserved) {
            throw new InvalidStockException("onHand cannot be < reserved after adjustment. onHand=" + newOnHand + ", reserved=" + reserved);
        }
        return new Stock((int) newOnHand, reserved);
    }
}
