package com.aredondocharro.ClothingStore.inventory.infrastructure.tx;

import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItemId;
import com.aredondocharro.ClothingStore.inventory.domain.model.ReservationReference;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.ReleaseStockUseCase;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

public class TransactionalReleaseStockUseCase implements ReleaseStockUseCase {

    private final ReleaseStockUseCase delegate;
    private final TransactionTemplate tx;

    public TransactionalReleaseStockUseCase(ReleaseStockUseCase delegate, TransactionTemplate tx) {
        this.delegate = delegate;
        this.tx = tx;
    }

    @Override
    public void release(InventoryItemId itemId, ReservationReference reference, Instant now) {
        tx.executeWithoutResult(status -> delegate.release(itemId, reference, now));
    }
}
