package com.aredondocharro.ClothingStore.shared.domain.event;

import java.time.Instant;
public interface DomainEvent {
    Instant occurredAt();
    String type();
}