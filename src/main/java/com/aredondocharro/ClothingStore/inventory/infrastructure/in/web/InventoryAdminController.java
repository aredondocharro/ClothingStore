package com.aredondocharro.ClothingStore.inventory.infrastructure.in.web;

import com.aredondocharro.ClothingStore.inventory.domain.model.*;
import com.aredondocharro.ClothingStore.inventory.domain.port.in.*;
import com.aredondocharro.ClothingStore.inventory.infrastructure.in.web.dto.*;
import com.aredondocharro.ClothingStore.inventory.infrastructure.in.web.mapper.InventoryWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/inventory/items")
public class InventoryAdminController {

    private final CreateInventoryItemUseCase createUseCase;
    private final UpdateInventoryItemUseCase updateUseCase;
    private final ChangeInventoryItemPriceUseCase changePriceUseCase;
    private final AdjustInventoryStockUseCase adjustStockUseCase;
    private final DiscontinueInventoryItemUseCase discontinueUseCase;
    private final ReserveStockUseCase reserveUseCase;
    private final ReleaseStockUseCase releaseUseCase;
    private final ConsumeStockUseCase consumeUseCase;

    @Operation(summary = "Create inventory item (admin)")
    @PostMapping
    public ResponseEntity<CreateInventoryItemResponse> create(@Valid @RequestBody CreateInventoryItemRequest body) {
        InventoryItemId id = createUseCase.create(InventoryWebMapper.toCreateCommand(body));
        return ResponseEntity
                .created(URI.create("/inventory/items/" + id.getValue()))
                .body(new CreateInventoryItemResponse(id.getValue()));
    }

    @Operation(summary = "Update inventory item details (admin)")
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable String id, @Valid @RequestBody UpdateInventoryItemRequest body) {
        InventoryItemId itemId = InventoryItemId.of(id);
        updateUseCase.update(InventoryWebMapper.toUpdateCommand(itemId, body));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Change inventory item price (admin)")
    @PatchMapping("/{id}/price")
    public ResponseEntity<Void> changePrice(@PathVariable String id, @Valid @RequestBody ChangePriceRequest body) {
        InventoryItemId itemId = InventoryItemId.of(id);
        Money money = Money.of(body.amount(), body.currency());
        changePriceUseCase.changePrice(itemId, money, null);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Adjust on-hand stock (admin)")
    @PostMapping("/{id}/stock/adjust")
    public ResponseEntity<Void> adjustStock(@PathVariable String id, @Valid @RequestBody AdjustStockRequest body) {
        InventoryItemId itemId = InventoryItemId.of(id);
        adjustStockUseCase.adjustOnHand(itemId, body.delta(), body.reason(), null);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Discontinue inventory item (admin)")
    @PostMapping("/{id}/discontinue")
    public ResponseEntity<Void> discontinue(@PathVariable String id) {
        InventoryItemId itemId = InventoryItemId.of(id);
        discontinueUseCase.discontinue(itemId, null);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reactivate inventory item (admin)")
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivate(@PathVariable String id) {
        InventoryItemId itemId = InventoryItemId.of(id);
        discontinueUseCase.reactivate(itemId, null);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reserve stock for reference (admin/system)")
    @PostMapping("/{id}/reserve")
    public ResponseEntity<ReserveStockResponse> reserve(@PathVariable String id, @Valid @RequestBody ReserveStockRequest body) {
        InventoryItemId itemId = InventoryItemId.of(id);
        ReservationReference reference = ReservationReference.of(body.reference());
        StockReservationId reservationId = reserveUseCase.reserve(itemId, reference, body.quantity(), null);
        return ResponseEntity.ok(new ReserveStockResponse(reservationId.getValue()));
    }

    @Operation(summary = "Release stock reservation by reference (admin/system)")
    @PostMapping("/{id}/release")
    public ResponseEntity<Void> release(@PathVariable String id, @Valid @RequestBody ReleaseStockRequest body) {
        InventoryItemId itemId = InventoryItemId.of(id);
        ReservationReference reference = ReservationReference.of(body.reference());
        releaseUseCase.release(itemId, reference, null);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Consume stock reservation after payment (admin/system)")
    @PostMapping("/{id}/consume")
    public ResponseEntity<Void> consume(@PathVariable String id, @Valid @RequestBody ConsumeStockRequest body) {
        InventoryItemId itemId = InventoryItemId.of(id);
        ReservationReference reference = ReservationReference.of(body.reference());
        consumeUseCase.consume(itemId, reference, null);
        return ResponseEntity.noContent().build();
    }
}
