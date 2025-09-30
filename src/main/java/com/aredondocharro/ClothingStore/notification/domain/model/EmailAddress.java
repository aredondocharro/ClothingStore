package com.aredondocharro.ClothingStore.notification.domain.model;

import com.aredondocharro.ClothingStore.notification.domain.exception.InvalidEmailAddressException;

public record EmailAddress(String value) {
    public EmailAddress {
        if (value == null || !value.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new InvalidEmailAddressException("Invalid email: " + value);
        }
    }
}
