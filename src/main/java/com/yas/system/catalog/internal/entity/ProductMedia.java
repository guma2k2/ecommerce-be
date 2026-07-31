package com.yas.system.catalog.internal.entity;

import com.yas.system.catalog.internal.dto.enumeration.ProductImageType;
import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import com.yas.system.common.entity.BaseLongEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_product_media")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductMedia extends BaseLongEntity {

    private int position;

    @Column(nullable = false)
    private String mediaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
}
