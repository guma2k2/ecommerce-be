package com.yas.system.payment.api.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record StripePromotionSyncCommand(
        Long promotionId,
        String name,
        String slug,
        String couponCode,
        String discountType,
        BigDecimal discountPercentage,
        BigDecimal discountAmount,
        String currency,
        BigDecimal minimumOrderPurchaseAmount,
        Integer usageLimit,
        ZonedDateTime endDate,
        boolean active,
        String existingStripeCouponId,
        String existingStripePromotionCodeId
) {
}
