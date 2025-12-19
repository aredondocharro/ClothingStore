package com.aredondocharro.ClothingStore.inventory.infrastructure.tx;

import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItemId;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.DiscontinueInventoryItemUseCase;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

public class TransactionalDiscontinueInventoryItemUseCase implements DiscontinueInventoryItemUseCase {

    private final DiscontinueInventoryItemUseCase delegate;
    private final TransactionTemplate tx;

    public TransactionalDiscontinueInventoryItemUseCase(DiscontinueInventoryItemUseCase delegate, TransactionTemplate tx) {
        this.delegate = delegate;
        this.tx = tx;
    }

    @Override
    public void discontinue(InventoryItemId id, Instant now) {
        tx.executeWithoutResult(status -> delegate.discontinue(id, now));
    }

    @Override
    public void reactivate(InventoryItemId id, Instant now) {
        tx.executeWithoutResult(status -> delegate.reactivate(id, now));
    }
}
