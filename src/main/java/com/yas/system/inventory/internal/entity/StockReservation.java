package com.yas.system.inventory.internal.entity;

import com.yas.system.common.entity.BaseLongEntity;
import com.yas.system.inventory.internal.enumeration.ReservationStatus;
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

import java.time.Instant;

@Entity
@Table(
        name = "tbl_stock_reservation",
        indexes = {
                @Index(name = "idx_reservation_order", columnList = "order_id"),
                @Index(name = "idx_reservation_variant", columnList = "product_variant_id"),
                @Index(name = "idx_reservation_status_expires", columnList = "status, expires_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReservation extends BaseLongEntity {

    @Column(name = "order_id", nullable = false, length = 36)
    private String orderId;

    @Column(name = "product_variant_id", nullable = false)
    private Long productVariantId;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}
