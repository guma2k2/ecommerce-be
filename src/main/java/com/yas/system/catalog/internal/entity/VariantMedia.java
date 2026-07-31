package com.yas.system.catalog.internal.entity;

import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import com.yas.system.common.entity.BaseLongEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_variant_media")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VariantMedia extends BaseLongEntity {

    @Column(nullable = false)
    private String mediaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;
}
