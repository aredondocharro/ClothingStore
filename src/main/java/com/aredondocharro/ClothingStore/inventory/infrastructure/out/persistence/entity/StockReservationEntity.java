package com.aredondocharro.ClothingStore.inventory.infrastructure.out.persistence.entity;

import com.aredondocharro.ClothingStore.inventory.domain.model.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "stock_reservations",
        uniqueConstraints = {
                // Permitimos 1 reserva por (item, reference, status). Esto evita duplicar ACTIVE
                // y deja histórico (RELEASED/CONSUMED). Para portfolio es suficiente.
                @UniqueConstraint(
                        name = "uk_stock_reservation_item_reference_status",
                        columnNames = {"item_id", "reference", "status"}
                )
        },
        indexes = {
                @Index(name = "idx_stock_reservation_item_ref_status", columnList = "item_id, reference, status")
        }
)
public class StockReservationEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "reference", nullable = false, length = 80)
    private String reference;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "released_at")
    private Instant releasedAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;
}
