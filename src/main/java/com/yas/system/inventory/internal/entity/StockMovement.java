package com.yas.system.inventory.internal.entity;

import com.yas.system.common.entity.BaseLongEntity;
import com.yas.system.inventory.internal.enumeration.MovementReason;
import com.yas.system.inventory.internal.enumeration.MovementType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "tbl_stock_movement",
        indexes = {
                @Index(name = "idx_stock_movement_variant", columnList = "product_variant_id"),
                @Index(name = "idx_stock_movement_ref", columnList = "reference_id"),
                @Index(name = "idx_stock_movement_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement extends BaseLongEntity {

    @Column(name = "inventory_id", nullable = false)
    private Long inventoryId;

    @Column(name = "product_variant_id", nullable = false)
    private Long productVariantId;

    @Column(name = "quantity_change", nullable = false)
    private int quantityChange;

    @Column(name = "quantity_before", nullable = false)
    private int quantityBefore;

    @Column(name = "quantity_after", nullable = false)
    private int quantityAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 30)
    private MovementType movementType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private MovementReason reason;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(length = 500)
    private String note;

    @Column(name = "performed_by", length = 100)
    private String performedBy;
}
