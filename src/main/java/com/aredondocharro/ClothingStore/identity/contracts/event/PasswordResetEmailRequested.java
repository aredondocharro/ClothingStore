package com.aredondocharro.ClothingStore.identity.contracts.event;

import com.aredondocharro.ClothingStore.shared.domain.event.DomainEvent;

import java.time.Instant;

public record PasswordResetEmailRequested(String email, String url, Instant occurredAt) implements DomainEvent {
    @Override
    public String type() {
        return "identity.password_reset_email.requested";
    }
}
