package com.aredondocharro.ClothingStore.shared.domain.event;

public interface EventBusPort {
    void publish(DomainEvent event);
}