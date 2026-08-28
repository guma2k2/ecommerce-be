package com.yas.system.catalog.internal.entity.attribute;

import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import com.yas.system.common.entity.BaseLongEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_variant_attribute_value")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductVariantAttributeValue extends BaseLongEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_attribute_id", nullable = false)
    private ProductAttribute productAttribute;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(nullable = false)
    private String value;
}
