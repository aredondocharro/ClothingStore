package com.aredondocharro.ClothingStore.inventory.domain.exception;

import com.aredondocharro.ClothingStore.inventory.domain.model.Sku;

public class SkuAlreadyExistsException extends RuntimeException {
    public SkuAlreadyExistsException(Sku sku) {
        super("SKU already exists: " + sku);
    }
}
