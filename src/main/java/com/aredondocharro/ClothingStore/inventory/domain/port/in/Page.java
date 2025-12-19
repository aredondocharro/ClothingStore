package com.aredondocharro.ClothingStore.inventory.domain.port.in;

import java.util.List;
import java.util.Objects;

public record Page<T>(List<T> items, long total, PageRequest request) {
    public Page {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(request, "request");
        if (total < 0) throw new IllegalArgumentException("total must be >= 0");
    }
}
