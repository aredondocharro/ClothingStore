package com.aredondocharro.ClothingStore.inventory.domain.model;

import com.aredondocharro.ClothingStore.inventory.domain.exception.InvalidMoneyException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) {

    public Money {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");

        if (amount.scale() > 2) {
            amount = amount.setScale(2, RoundingMode.HALF_UP);
        } else {
            amount = amount.setScale(2, RoundingMode.UNNECESSARY);
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidMoneyException("Money amount must be >= 0");
        }
    }

    public static Money eur(BigDecimal amount) {
        return new Money(amount, Currency.getInstance("EUR"));
    }

    public static Money of(BigDecimal amount, String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new InvalidMoneyException("Currency is required");
        }
        try {
            return new Money(amount, Currency.getInstance(currencyCode.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new InvalidMoneyException("Invalid currency: " + currencyCode);
        }
    }
}
