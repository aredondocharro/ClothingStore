package com.aredondocharro.ClothingStore.inventory.infrastructure.tx;

import com.aredondocharro.ClothingStore.inventory.domain.model.InventoryItemId;
import com.aredondocharro.ClothingStore.inventory.domain.model.Money;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.ChangeInventoryItemPriceUseCase;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

public class TransactionalChangeInventoryItemPriceUseCase implements ChangeInventoryItemPriceUseCase {

    private final ChangeInventoryItemPriceUseCase delegate;
    private final TransactionTemplate tx;

    public TransactionalChangeInventoryItemPriceUseCase(ChangeInventoryItemPriceUseCase delegate, TransactionTemplate tx) {
        this.delegate = delegate;
        this.tx = tx;
    }

    @Override
    public void changePrice(InventoryItemId id, Money newPrice, Instant now) {
        tx.executeWithoutResult(status -> delegate.changePrice(id, newPrice, now));
    }
}
