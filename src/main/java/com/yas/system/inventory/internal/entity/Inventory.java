package com.yas.system.inventory.internal.entity;

import com.yas.system.common.entity.BaseLongEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "tbl_inventory",
        indexes = {
                @Index(name = "idx_inventory_variant_id", columnList = "product_variant_id", unique = true),
                @Index(name = "idx_inventory_sku", columnList = "sku")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory extends BaseLongEntity {

    @Column(name = "product_variant_id", nullable = false, unique = true)
    private Long productVariantId;

    @Column(length = 64)
    private String sku;

    @Builder.Default
    @Column(name = "on_hand", nullable = false)
    private int onHand = 0;

    @Builder.Default
    @Column(name = "reserved", nullable = false)
    private int reserved = 0;

    @Version
    private Long version;

    public int getAvailable() {
        return Math.max(0, onHand - reserved);
    }
}
