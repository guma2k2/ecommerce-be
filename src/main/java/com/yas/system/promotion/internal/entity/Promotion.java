package com.yas.system.promotion.internal.entity;

import com.yas.system.common.entity.BaseLongEntity;
import com.yas.system.promotion.internal.enumeration.ApplyTo;
import com.yas.system.promotion.internal.enumeration.DiscountType;
import com.yas.system.promotion.internal.enumeration.UsageType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "tbl_promotion",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_promotion_slug", columnNames = "slug"),
                @UniqueConstraint(name = "uk_promotion_coupon_code", columnNames = "coupon_code")
        },
        indexes = {
                @Index(name = "idx_promotion_coupon_code", columnList = "coupon_code"),
                @Index(name = "idx_promotion_active_dates", columnList = "is_active, start_date, end_date")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Promotion extends BaseLongEntity {

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 100, nullable = false)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "coupon_code", length = 50, nullable = false)
    private String couponCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", length = 30, nullable = false)
    private DiscountType discountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage_type", length = 30, nullable = false)
    private UsageType usageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "apply_to", length = 30, nullable = false)
    private ApplyTo applyTo;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_count", nullable = false)
    @ColumnDefault("0")
    @Builder.Default
    private int usageCount = 0;

    @Column(name = "usage_limit_per_customer")
    @ColumnDefault("1")
    @Builder.Default
    private Integer usageLimitPerCustomer = 1;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "max_discount_amount", precision = 12, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "minimum_order_purchase_amount", precision = 12, scale = 2)
    private BigDecimal minimumOrderPurchaseAmount;

    @Column(name = "is_active", nullable = false)
    @ColumnDefault("true")
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "start_date", nullable = false)
    private ZonedDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private ZonedDateTime endDate;

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PromotionApply> applies = new ArrayList<>();
}
