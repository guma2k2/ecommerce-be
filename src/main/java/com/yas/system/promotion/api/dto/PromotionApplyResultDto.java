package com.yas.system.promotion.api.dto;

import java.math.BigDecimal;

public record PromotionApplyResultDto(
        Long promotionId,
        String couponCode,
        BigDecimal discountAmount,
        String stripeCouponId,
        String stripePromotionCodeId
) {
    public PromotionApplyResultDto(Long promotionId, String couponCode, BigDecimal discountAmount) {
        this(promotionId, couponCode, discountAmount, null, null);
    }
}
