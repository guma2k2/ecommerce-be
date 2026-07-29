package com.yas.system.catalog.internal.entity.variant;

import com.yas.system.catalog.internal.entity.option.ProductOption;
import com.yas.system.common.entity.BaseLongEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_variant_option_value",
        uniqueConstraints = @UniqueConstraint(
        columnNames = {
                "product_variant_id",
                "attribute_id"
        }
))
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VariantOptionValue extends BaseLongEntity {
    
    private String value;

    private int position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id")
    private ProductOption productOption;
}
