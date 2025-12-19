package com.aredondocharro.ClothingStore.inventory.infrastructure.in.web;

import com.aredondocharro.ClothingStore.inventory.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestControllerAdvice(assignableTypes = {InventoryPublicController.class, InventoryAdminController.class})
public class InventoryExceptionHandler {

    @ExceptionHandler(InventoryItemNotFoundException.class)
    public ResponseEntity<InventoryErrorResponse> notFound(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "inventory.item.not_found", ex.getMessage(), req);
    }

    @ExceptionHandler({SkuAlreadyExistsException.class, StockReservationAlreadyExistsException.class})
    public ResponseEntity<InventoryErrorResponse> conflict(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "inventory.conflict", ex.getMessage(), req);
    }

    @ExceptionHandler({InsufficientStockException.class, ItemDiscontinuedException.class})
    public ResponseEntity<InventoryErrorResponse> businessConflict(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "inventory.business_conflict", ex.getMessage(), req);
    }

    @ExceptionHandler({
            InvalidSkuException.class,
            InvalidMoneyException.class,
            InvalidStockException.class,
            InvalidReservationException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<InventoryErrorResponse> badRequest(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "inventory.bad_request", ex.getMessage(), req);
    }

    @ExceptionHandler({StockReservationNotFoundException.class, StockReservationNotActiveException.class})
    public ResponseEntity<InventoryErrorResponse> reservationConflict(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "inventory.reservation.conflict", ex.getMessage(), req);
    }


    private ResponseEntity<InventoryErrorResponse> build(HttpStatus status, String code, String message, HttpServletRequest req) {
        return ResponseEntity.status(status).body(new InventoryErrorResponse(
                Instant.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                req.getRequestURI()
        ));
    }

    public record InventoryErrorResponse(
            String timestamp,
            int status,
            String error,
            String code,
            String message,
            String path
    ) {}
}
