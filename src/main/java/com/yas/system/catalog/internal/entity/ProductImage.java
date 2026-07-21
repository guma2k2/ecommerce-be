package com.yas.system.catalog.internal.entity;

import com.yas.system.catalog.internal.dto.enums.ProductImageType;
import com.yas.system.common.entity.BaseLongEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_product_image")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductImage extends BaseLongEntity {

    private String url;

    @Enumerated(EnumType.STRING)
    private ProductImageType type;

    @Builder.Default
    private int position = 0;

    private boolean status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

}
