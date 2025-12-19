package com.aredondocharro.ClothingStore.inventory.infrastructure.tx;

import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItemId;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.AdjustInventoryStockUseCase;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

public class TransactionalAdjustInventoryStockUseCase implements AdjustInventoryStockUseCase {

    private final AdjustInventoryStockUseCase delegate;
    private final TransactionTemplate tx;

    public TransactionalAdjustInventoryStockUseCase(AdjustInventoryStockUseCase delegate, TransactionTemplate tx) {
        this.delegate = delegate;
        this.tx = tx;
    }

    @Override
    public void adjustOnHand(InventoryItemId id, int delta, String reason, Instant now) {
        tx.executeWithoutResult(status -> delegate.adjustOnHand(id, delta, reason, now));
    }
}
