package com.aredondocharro.ClothingStore.inventory.infrastructure.config.usecase;

import com.aredondocharro.ClothingStore.inventory.application.*;
import com.aredondocharro.ClothingStore.inventory.domain.port.out.InventoryItemRepositoryPort;
import com.aredondocharro.ClothingStore.inventory.domain.port.out.StockReservationRepositoryPort;
import com.aredondocharro.ClothingStore.shared.domain.event.EventBusPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class InventoryUseCasesConfig {

    @Bean
    @ConditionalOnMissingBean(Clock.class)
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    CreateInventoryItemService createInventoryItemService(InventoryItemRepositoryPort repo, Clock clock, EventBusPort eventBus) {
        return new CreateInventoryItemService(repo, clock, eventBus);
    }

    @Bean
    UpdateInventoryItemService updateInventoryItemService(InventoryItemRepositoryPort repo, Clock clock) {
        return new UpdateInventoryItemService(repo, clock);
    }

    @Bean
    ChangeInventoryItemPriceService changeInventoryItemPriceService(InventoryItemRepositoryPort repo, Clock clock, EventBusPort eventBus) {
        return new ChangeInventoryItemPriceService(repo, clock, eventBus);
    }

    @Bean
    AdjustInventoryStockService adjustInventoryStockService(InventoryItemRepositoryPort repo, Clock clock, EventBusPort eventBus) {
        return new AdjustInventoryStockService(repo, clock, eventBus);
    }

    @Bean
    DiscontinueInventoryItemService discontinueInventoryItemService(InventoryItemRepositoryPort repo, Clock clock, EventBusPort eventBus) {
        return new DiscontinueInventoryItemService(repo, clock, eventBus);
    }

    @Bean
    GetInventoryItemService getInventoryItemService(InventoryItemRepositoryPort repo) {
        return new GetInventoryItemService(repo);
    }

    @Bean
    SearchInventoryItemsService searchInventoryItemsService(InventoryItemRepositoryPort repo) {
        return new SearchInventoryItemsService(repo);
    }

    @Bean
    ReserveStockService reserveStockService(InventoryItemRepositoryPort itemRepo,
                                            StockReservationRepositoryPort reservationRepo,
                                            Clock clock,
                                            EventBusPort eventBus) {
        return new ReserveStockService(itemRepo, reservationRepo, clock, eventBus);
    }

    @Bean
    ReleaseStockService releaseStockService(InventoryItemRepositoryPort itemRepo,
                                            StockReservationRepositoryPort reservationRepo,
                                            Clock clock,
                                            EventBusPort eventBus) {
        return new ReleaseStockService(itemRepo, reservationRepo, clock, eventBus);
    }

    @Bean
    ConsumeStockService consumeStockService(InventoryItemRepositoryPort itemRepo,
                                            StockReservationRepositoryPort reservationRepo,
                                            Clock clock) {
        return new ConsumeStockService(itemRepo, reservationRepo, clock);
    }
}
