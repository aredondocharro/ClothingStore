package com.aredondocharro.ClothingStore.inventory.infrastructure.tx;

import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItemId;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.CreateInventoryItemCommand;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.CreateInventoryItemUseCase;
import org.springframework.transaction.support.TransactionTemplate;

public class TransactionalCreateInventoryItemUseCase implements CreateInventoryItemUseCase {

    private final CreateInventoryItemUseCase delegate;
    private final TransactionTemplate tx;

    public TransactionalCreateInventoryItemUseCase(CreateInventoryItemUseCase delegate, TransactionTemplate tx) {
        this.delegate = delegate;
        this.tx = tx;
    }

    @Override
    public InventoryItemId create(CreateInventoryItemCommand command) {
        return tx.execute(status -> delegate.create(command));
    }
}
