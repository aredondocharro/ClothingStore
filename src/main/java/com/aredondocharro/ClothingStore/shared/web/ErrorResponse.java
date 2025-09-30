package com.aredondocharro.ClothingStore.shared.web;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,     // "Bad Request", "Unauthorized", ...
        String code,      // app-specific: "identity.invalid_credentials"
        String message,   // mensaje legible
        String path,      // request URI
        List<FieldError> fieldErrors // para validaciones
) {
    public record FieldError(String field, String message) {}
}
