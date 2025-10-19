package com.aredondocharro.ClothingStore.identity.contracts.event;

import com.aredondocharro.ClothingStore.identity.domain.model.IdentityEmail;
import com.aredondocharro.ClothingStore.identity.domain.model.UserId;
import com.aredondocharro.ClothingStore.shared.domain.event.DomainEvent;

import java.time.Instant;

public record UserRegistered(UserId userId, IdentityEmail email, Instant occurredAt) implements DomainEvent {
    @Override public String type() { return "identity.user.registered"; }
}