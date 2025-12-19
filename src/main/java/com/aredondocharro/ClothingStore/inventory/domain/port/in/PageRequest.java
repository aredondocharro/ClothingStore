package com.aredondocharro.ClothingStore.inventory.domain.port.in;

public record PageRequest(int offset, int limit) {
    public PageRequest {
        if (offset < 0) throw new IllegalArgumentException("offset must be >= 0");
        if (limit <= 0) throw new IllegalArgumentException("limit must be > 0");
        if (limit > 200) throw new IllegalArgumentException("limit max is 200");
    }

    public static PageRequest of(int offset, int limit) {
        return new PageRequest(offset, limit);
    }
}
