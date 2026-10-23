package com.yas.system.payment.api.dto;

public record StripePromotionSyncResult(
        Long promotionId,
        String stripeCouponId,
        String stripePromotionCodeId
) {
}
