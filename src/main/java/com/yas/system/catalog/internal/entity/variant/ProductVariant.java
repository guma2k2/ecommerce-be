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
@Table(
        name = "tbl_product_variant",
        indexes = {
                @Index(name = "idx_product_variant_stripe_prod", columnList = "stripe_product_id"),
                @Index(name = "idx_product_variant_stripe_price", columnList = "stripe_price_id")
        }
)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductVariant extends BaseLongEntity {
    @Column(length = 150)
    private String title;

    @Column(nullable = false, length = 64)
    private String sku;

    private BigDecimal price;
    private Integer quantity;

    @Column(length = 36)
    private String mediaId;

    @Column(name = "stripe_product_id", length = 100)
    private String stripeProductId;

    @Column(name = "stripe_price_id", length = 100)
    private String stripePriceId;

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
