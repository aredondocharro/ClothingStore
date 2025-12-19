package com.aredondocharro.ClothingStore.inventory.infrastructure.tx;

import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItemId;
import com.aredondocharro.ClothingStore.inventory.domain.model.ReservationReference;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.ConsumeStockUseCase;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

public class TransactionalConsumeStockUseCase implements ConsumeStockUseCase {

    private final ConsumeStockUseCase delegate;
    private final TransactionTemplate tx;

    public TransactionalConsumeStockUseCase(ConsumeStockUseCase delegate, TransactionTemplate tx) {
        this.delegate = delegate;
        this.tx = tx;
    }

    @Override
    public void consume(InventoryItemId itemId, ReservationReference reference, Instant now) {
        tx.executeWithoutResult(status -> delegate.consume(itemId, reference, now));
    }
}
