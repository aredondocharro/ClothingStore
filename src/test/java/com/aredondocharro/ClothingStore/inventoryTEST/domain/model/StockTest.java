package com.aredondocharro.ClothingStore.inventoryTEST.domain.model;

import com.aredondocharro.ClothingStore.inventory.domain.exception.InsufficientStockException;
import com.aredondocharro.ClothingStore.inventory.domain.exception.InvalidStockException;
import com.aredondocharro.ClothingStore.inventory.domain.model.Stock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StockTest {

    @Test
    void cannotCreateNegativeStock() {
        assertThrows(InvalidStockException.class, () -> new Stock(-1, 0));
        assertThrows(InvalidStockException.class, () -> new Stock(10, -1));
    }

    @Test
    void reservedCannotExceedOnHand() {
        assertThrows(InvalidStockException.class, () -> new Stock(5, 6));
    }

    @Test
    void reserveConsumesAvailable() {
        Stock s = new Stock(10, 2);
        Stock r = s.reserve(3);

        assertEquals(10, r.onHand());
        assertEquals(5, r.reserved());
        assertEquals(5, r.available());
    }

    @Test
    void reserveFailsWhenNotEnoughAvailable() {
        Stock s = new Stock(10, 9);
        assertThrows(InsufficientStockException.class, () -> s.reserve(2));
    }

    @Test
    void releaseCannotGoBelowZero() {
        Stock s = new Stock(10, 2);
        assertThrows(InvalidStockException.class, () -> s.release(3));
    }

    @Test
    void adjustOnHandCannotGoBelowReserved() {
        Stock s = new Stock(10, 7);
        assertThrows(InvalidStockException.class, () -> s.adjustOnHand(-4)); // 6 < reserved(7)
    }
}
