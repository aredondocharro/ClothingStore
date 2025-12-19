package com.aredondocharro.ClothingStore.inventory.infrastructure.tx;

import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItemId;
import com.aredondocharro.ClothingStore.inventory.domain.model.ReservationReference;
import com.aredondocharro.ClothingStore.inventory.domain.model.StockReservationId;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.ReserveStockUseCase;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

public class TransactionalReserveStockUseCase implements ReserveStockUseCase {

    private final ReserveStockUseCase delegate;
    private final TransactionTemplate tx;

    public TransactionalReserveStockUseCase(ReserveStockUseCase delegate, TransactionTemplate tx) {
        this.delegate = delegate;
        this.tx = tx;
    }

    @Override
    public StockReservationId reserve(InventoryItemId itemId, ReservationReference reference, int quantity, Instant now) {
        return tx.execute(status -> delegate.reserve(itemId, reference, quantity, now));
    }
}
