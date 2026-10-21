package com.yas.system.promotion.internal.dto.response;

import com.yas.system.promotion.internal.enumeration.ApplyTo;
import com.yas.system.promotion.internal.enumeration.DiscountType;

import java.math.BigDecimal;
import java.util.List;

public record PromotionVerifyResponse(
        boolean isValid,
        String couponCode,
        Long promotionId,
        String promotionName,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal discountAmount,
        ApplyTo applyTo,
        List<Long> eligibleVariantIds
) {
}
