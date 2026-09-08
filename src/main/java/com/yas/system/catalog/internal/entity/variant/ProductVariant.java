package com.yas.system.catalog.internal.entity.variant;

import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.attribute.ProductVariantAttributeValue;
import com.yas.system.common.entity.BaseLongEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_product_variant")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductVariant extends BaseLongEntity {
    private String title;
    private String sku;
    private BigDecimal price;
    private Integer quantity;
    private String status;
    private String mediaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Builder.Default
    @OneToMany(mappedBy = "productVariant")
    private List<VariantOptionValue> variantOptionValues = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "productVariant")
    private List<ProductVariantAttributeValue> attributeValues = new ArrayList<>();
}
