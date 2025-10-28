package com.aredondocharro.ClothingStore.identity.contracts.event;

import com.aredondocharro.ClothingStore.shared.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record UserRegistered(UUID userId, Instant occurredAt) implements DomainEvent {
    @Override public String type() { return "identity.user.registered"; }
}