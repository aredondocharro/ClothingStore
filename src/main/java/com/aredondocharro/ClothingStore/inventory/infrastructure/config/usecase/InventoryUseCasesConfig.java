package com.aredondocharro.ClothingStore.inventory.infrastructure.config.usecase;

import com.aredondocharro.ClothingStore.inventory.application.*;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.*;
import com.aredondocharro.ClothingStore.inventory.domain.port.out.InventoryItemRepositoryPort;
import com.aredondocharro.ClothingStore.inventory.domain.port.out.StockReservationRepositoryPort;
import com.aredondocharro.ClothingStore.shared.domain.event.EventBusPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class InventoryUseCasesConfig {

    // -------------------------
    // WRITE use cases (core)
    // -------------------------

    @Bean(name = "createInventoryItemUseCaseCore")
    public CreateInventoryItemUseCase createInventoryItemUseCaseCore(
            InventoryItemRepositoryPort repo,
            Clock clock,
            EventBusPort eventBus
    ) {
        return new CreateInventoryItemService(repo, clock, eventBus);
    }

    @Bean(name = "updateInventoryItemUseCaseCore")
    public UpdateInventoryItemUseCase updateInventoryItemUseCaseCore(
            InventoryItemRepositoryPort repo,
            Clock clock
    ) {
        return new UpdateInventoryItemService(repo, clock);
    }

    @Bean(name = "changeInventoryItemPriceUseCaseCore")
    public ChangeInventoryItemPriceUseCase changeInventoryItemPriceUseCaseCore(
            InventoryItemRepositoryPort repo,
            Clock clock,
            EventBusPort eventBus
    ) {
        return new ChangeInventoryItemPriceService(repo, clock, eventBus);
    }

    @Bean(name = "adjustInventoryStockUseCaseCore")
    public AdjustInventoryStockUseCase adjustInventoryStockUseCaseCore(
            InventoryItemRepositoryPort repo,
            Clock clock,
            EventBusPort eventBus
    ) {
        return new AdjustInventoryStockService(repo, clock, eventBus);
    }

    @Bean(name = "discontinueInventoryItemUseCaseCore")
    public DiscontinueInventoryItemUseCase discontinueInventoryItemUseCaseCore(
            InventoryItemRepositoryPort repo,
            Clock clock,
            EventBusPort eventBus
    ) {
        return new DiscontinueInventoryItemService(repo, clock, eventBus);
    }

    // -------------------------
    // READ use cases (core)
    // -------------------------

    @Bean(name = "getInventoryItemUseCaseCore")
    public GetInventoryItemUseCase getInventoryItemUseCaseCore(InventoryItemRepositoryPort repo) {
        return new GetInventoryItemService(repo);
    }

    @Bean(name = "searchInventoryItemsUseCaseCore")
    public SearchInventoryItemsUseCase searchInventoryItemsUseCaseCore(InventoryItemRepositoryPort repo) {
        return new SearchInventoryItemsService(repo);
    }

    // -------------------------
    // STOCK reservation flow (core)
    // -------------------------

    @Bean(name = "reserveStockUseCaseCore")
    public ReserveStockUseCase reserveStockUseCaseCore(
            InventoryItemRepositoryPort itemRepo,
            StockReservationRepositoryPort reservationRepo,
            Clock clock,
            EventBusPort eventBus
    ) {
        return new ReserveStockService(itemRepo, reservationRepo, clock, eventBus);
    }

    @Bean(name = "releaseStockUseCaseCore")
    public ReleaseStockUseCase releaseStockUseCaseCore(
            InventoryItemRepositoryPort itemRepo,
            StockReservationRepositoryPort reservationRepo,
            Clock clock,
            EventBusPort eventBus
    ) {
        return new ReleaseStockService(itemRepo, reservationRepo, clock, eventBus);
    }

    @Bean(name = "consumeStockUseCaseCore")
    public ConsumeStockUseCase consumeStockUseCaseCore(
            InventoryItemRepositoryPort itemRepo,
            StockReservationRepositoryPort reservationRepo,
            Clock clock
    ) {
        return new ConsumeStockService(itemRepo, reservationRepo, clock);
    }
}
