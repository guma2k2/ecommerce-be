package com.yas.system.promotion.internal.entity;

import com.yas.system.common.entity.BaseLongEntity;
import com.yas.system.promotion.internal.enumeration.PromotionUsageStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "tbl_promotion_usage",
        indexes = {
                @Index(name = "idx_promotion_usage_order_id", columnList = "order_id"),
                @Index(name = "idx_promotion_usage_customer_promo", columnList = "customer_id, promotion_id"),
                @Index(name = "idx_promotion_usage_status", columnList = "status")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionUsage extends BaseLongEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Column(name = "customer_id", length = 36, nullable = false)
    private String customerId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "discount_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal discountAmount;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    @ColumnDefault("'APPLIED'")
    @Builder.Default
    private PromotionUsageStatus status = PromotionUsageStatus.APPLIED;
}
