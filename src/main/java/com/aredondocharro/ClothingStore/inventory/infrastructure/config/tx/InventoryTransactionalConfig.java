package com.aredondocharro.ClothingStore.inventory.infrastructure.config.tx;

import com.aredondocharro.ClothingStore.inventory.application.*;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.*;
import com.aredondocharro.ClothingStore.inventory.infrastructure.tx.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class InventoryTransactionalConfig {

    private TransactionTemplate tx(PlatformTransactionManager tm) {
        return new TransactionTemplate(tm);
    }

    @Bean
    CreateInventoryItemUseCase createInventoryItemUseCase(CreateInventoryItemService svc, PlatformTransactionManager tm) {
        return new TransactionalCreateInventoryItemUseCase(svc, tx(tm));
    }

    @Bean
    UpdateInventoryItemUseCase updateInventoryItemUseCase(UpdateInventoryItemService svc, PlatformTransactionManager tm) {
        return new TransactionalUpdateInventoryItemUseCase(svc, tx(tm));
    }

    @Bean
    ChangeInventoryItemPriceUseCase changeInventoryItemPriceUseCase(ChangeInventoryItemPriceService svc, PlatformTransactionManager tm) {
        return new TransactionalChangeInventoryItemPriceUseCase(svc, tx(tm));
    }

    @Bean
    AdjustInventoryStockUseCase adjustInventoryStockUseCase(AdjustInventoryStockService svc, PlatformTransactionManager tm) {
        return new TransactionalAdjustInventoryStockUseCase(svc, tx(tm));
    }

    @Bean
    DiscontinueInventoryItemUseCase discontinueInventoryItemUseCase(DiscontinueInventoryItemService svc, PlatformTransactionManager tm) {
        return new TransactionalDiscontinueInventoryItemUseCase(svc, tx(tm));
    }

    // Reads: puedes exponer directamente sin wrapper
    @Bean
    GetInventoryItemUseCase getInventoryItemUseCase(GetInventoryItemService svc) { return svc; }

    @Bean
    SearchInventoryItemsUseCase searchInventoryItemsUseCase(SearchInventoryItemsService svc) { return svc; }

    @Bean
    ReserveStockUseCase reserveStockUseCase(ReserveStockService svc, PlatformTransactionManager tm) {
        return new TransactionalReserveStockUseCase(svc, tx(tm));
    }

    @Bean
    ReleaseStockUseCase releaseStockUseCase(ReleaseStockService svc, PlatformTransactionManager tm) {
        return new TransactionalReleaseStockUseCase(svc, tx(tm));
    }

    @Bean
    ConsumeStockUseCase consumeStockUseCase(ConsumeStockService svc, PlatformTransactionManager tm) {
        return new TransactionalConsumeStockUseCase(svc, tx(tm));
    }
}
