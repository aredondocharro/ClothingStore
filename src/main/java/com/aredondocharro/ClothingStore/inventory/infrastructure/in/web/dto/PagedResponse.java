package com.aredondocharro.ClothingStore.inventory.infrastructure.in.web.dto;

import java.util.List;

public record PagedResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalItems,
        int totalPages
) {}
