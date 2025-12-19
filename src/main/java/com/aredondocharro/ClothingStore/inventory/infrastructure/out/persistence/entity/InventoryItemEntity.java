package com.aredondocharro.ClothingStore.inventory.infrastructure.out.persistence.entity;

import com.aredondocharro.ClothingStore.inventory.domain.model.*;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "inventory_items",
        indexes = {
                @Index(name = "idx_inventory_items_sku", columnList = "sku", unique = true),
                @Index(name = "idx_inventory_items_status", columnList = "status")
        }
)
public class InventoryItemEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "sku", nullable = false, length = 64, unique = true)
    private String sku;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private InventoryCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "accessory_type", nullable = false, length = 32)
    private AccessoryType accessoryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 16)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "size", nullable = false, length = 16)
    private Size size;

    @Enumerated(EnumType.STRING)
    @Column(name = "fabric", nullable = false, length = 32)
    private Fabric fabric;

    @Column(name = "color", length = 40)
    private String color;

    @Column(name = "price_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal priceAmount;

    @Column(name = "price_currency", nullable = false, length = 3)
    private String priceCurrency;

    @Column(name = "stock_on_hand", nullable = false)
    private int stockOnHand;

    @Column(name = "stock_reserved", nullable = false)
    private int stockReserved;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private ItemStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
