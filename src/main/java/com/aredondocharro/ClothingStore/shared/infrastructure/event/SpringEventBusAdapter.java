package com.aredondocharro.ClothingStore.shared.infrastructure.event;

import com.aredondocharro.ClothingStore.shared.domain.event.DomainEvent;
import com.aredondocharro.ClothingStore.shared.domain.event.EventBusPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringEventBusAdapter implements EventBusPort {
    private final ApplicationEventPublisher publisher;
    @Override public void publish(DomainEvent event){ publisher.publishEvent(event); }
}
