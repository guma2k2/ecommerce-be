package com.yas.system.promotion.internal.entity;

import com.yas.system.common.entity.BaseLongEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "tbl_promotion_apply",
        indexes = {
                @Index(name = "idx_promotion_apply_promotion_id", columnList = "promotion_id"),
                @Index(name = "idx_promotion_apply_product_id", columnList = "product_id"),
                @Index(name = "idx_promotion_apply_category_id", columnList = "category_id"),
                @Index(name = "idx_promotion_apply_brand_id", columnList = "brand_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionApply extends BaseLongEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "brand_id")
    private Long brandId;
}
