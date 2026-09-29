package com.yas.system.cart.internal.entity;

import com.yas.system.common.entity.BaseLongEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

@Entity
@Table(
        name = "tbl_cart",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_cart_customer_variant", columnNames = {"customer_id", "product_variant_id"})
        }
)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Cart extends BaseLongEntity {

    @Column(name = "product_variant_id", nullable = false)
    private Long productVariantId;

    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;

    @Column(nullable = false)
    private int quantity;
}
