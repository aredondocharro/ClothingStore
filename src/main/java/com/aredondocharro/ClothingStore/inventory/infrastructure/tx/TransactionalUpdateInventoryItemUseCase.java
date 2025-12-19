package com.aredondocharro.ClothingStore.inventory.infrastructure.tx;

import com.aredondocharro.ClothingStore.inventory.domain.port.in.UpdateInventoryItemCommand;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.UpdateInventoryItemUseCase;
import org.springframework.transaction.support.TransactionTemplate;

public class TransactionalUpdateInventoryItemUseCase implements UpdateInventoryItemUseCase {

    private final UpdateInventoryItemUseCase delegate;
    private final TransactionTemplate tx;

    public TransactionalUpdateInventoryItemUseCase(UpdateInventoryItemUseCase delegate, TransactionTemplate tx) {
        this.delegate = delegate;
        this.tx = tx;
    }

    @Override
    public void update(UpdateInventoryItemCommand command) {
        tx.executeWithoutResult(status -> delegate.update(command));
    }
}
