package com.aredondocharro.ClothingStore.inventory.infrastructure.config.tx;

import com.aredondocharro.ClothingStore.inventory.domain.port.in.*;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

@Configuration(proxyBeanMethods = false)
public class InventoryTransactionalConfig {

    // -------------------------
    // WRITE use cases (tx)
    // -------------------------

    @Bean
    @Primary
    public CreateInventoryItemUseCase createInventoryItemUseCase(
            @Qualifier("transactionManager") PlatformTransactionManager txManager,
            @Qualifier("createInventoryItemUseCaseCore") CreateInventoryItemUseCase core
    ) {
        return txProxy(CreateInventoryItemUseCase.class, core, txManager, false);
    }

    @Bean
    @Primary
    public UpdateInventoryItemUseCase updateInventoryItemUseCase(
            @Qualifier("transactionManager") PlatformTransactionManager txManager,
            @Qualifier("updateInventoryItemUseCaseCore") UpdateInventoryItemUseCase core
    ) {
        return txProxy(UpdateInventoryItemUseCase.class, core, txManager, false);
    }

    @Bean
    @Primary
    public ChangeInventoryItemPriceUseCase changeInventoryItemPriceUseCase(
            @Qualifier("transactionManager") PlatformTransactionManager txManager,
            @Qualifier("changeInventoryItemPriceUseCaseCore") ChangeInventoryItemPriceUseCase core
    ) {
        return txProxy(ChangeInventoryItemPriceUseCase.class, core, txManager, false);
    }

    @Bean
    @Primary
    public AdjustInventoryStockUseCase adjustInventoryStockUseCase(
            @Qualifier("transactionManager") PlatformTransactionManager txManager,
            @Qualifier("adjustInventoryStockUseCaseCore") AdjustInventoryStockUseCase core
    ) {
        return txProxy(AdjustInventoryStockUseCase.class, core, txManager, false);
    }

    @Bean
    @Primary
    public DiscontinueInventoryItemUseCase discontinueInventoryItemUseCase(
            @Qualifier("transactionManager") PlatformTransactionManager txManager,
            @Qualifier("discontinueInventoryItemUseCaseCore") DiscontinueInventoryItemUseCase core
    ) {
        return txProxy(DiscontinueInventoryItemUseCase.class, core, txManager, false);
    }

    // -------------------------
    // READ use cases (tx, readOnly)
    // -------------------------

    @Bean
    @Primary
    public GetInventoryItemUseCase getInventoryItemUseCase(
            @Qualifier("transactionManager") PlatformTransactionManager txManager,
            @Qualifier("getInventoryItemUseCaseCore") GetInventoryItemUseCase core
    ) {
        return txProxy(GetInventoryItemUseCase.class, core, txManager, true);
    }

    @Bean
    @Primary
    public SearchInventoryItemsUseCase searchInventoryItemsUseCase(
            @Qualifier("transactionManager") PlatformTransactionManager txManager,
            @Qualifier("searchInventoryItemsUseCaseCore") SearchInventoryItemsUseCase core
    ) {
        return txProxy(SearchInventoryItemsUseCase.class, core, txManager, true);
    }

    // -------------------------
    // STOCK reservation flow (tx)
    // -------------------------

    @Bean
    @Primary
    public ReserveStockUseCase reserveStockUseCase(
            @Qualifier("transactionManager") PlatformTransactionManager txManager,
            @Qualifier("reserveStockUseCaseCore") ReserveStockUseCase core
    ) {
        return txProxy(ReserveStockUseCase.class, core, txManager, false);
    }

    @Bean
    @Primary
    public ReleaseStockUseCase releaseStockUseCase(
            @Qualifier("transactionManager") PlatformTransactionManager txManager,
            @Qualifier("releaseStockUseCaseCore") ReleaseStockUseCase core
    ) {
        return txProxy(ReleaseStockUseCase.class, core, txManager, false);
    }

    @Bean
    @Primary
    public ConsumeStockUseCase consumeStockUseCase(
            @Qualifier("transactionManager") PlatformTransactionManager txManager,
            @Qualifier("consumeStockUseCaseCore") ConsumeStockUseCase core
    ) {
        return txProxy(ConsumeStockUseCase.class, core, txManager, false);
    }

    // -------------------------
    // Internal helper
    // -------------------------

    private static <T> T txProxy(
            Class<T> iface,
            T target,
            PlatformTransactionManager txManager,
            boolean readOnly
    ) {
        RuleBasedTransactionAttribute attr = new RuleBasedTransactionAttribute();
        attr.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        attr.setReadOnly(readOnly);

        NameMatchTransactionAttributeSource tas = new NameMatchTransactionAttributeSource();
        tas.addTransactionalMethod("*", attr);

        TransactionInterceptor interceptor = new TransactionInterceptor(txManager, tas);

        ProxyFactory pf = new ProxyFactory();
        pf.setTarget(target);
        pf.setInterfaces(iface);
        pf.addAdvice(interceptor);

        return iface.cast(pf.getProxy());
    }
}
