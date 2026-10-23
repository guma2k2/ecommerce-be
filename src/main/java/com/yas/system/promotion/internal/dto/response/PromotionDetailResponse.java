package com.yas.system.promotion.internal.dto.response;

import com.yas.system.promotion.internal.enumeration.ApplyTo;
import com.yas.system.promotion.internal.enumeration.DiscountType;
import com.yas.system.promotion.internal.enumeration.UsageType;

import java.math.BigDecimal;
import java.util.List;

public record PromotionDetailResponse(
        Long id,
        String name,
        String slug,
        String description,
        String couponCode,
        DiscountType discountType,
        UsageType usageType,
        ApplyTo applyTo,
        Integer usageLimit,
        int usageCount,
        Integer usageLimitPerCustomer,
        BigDecimal discountPercentage,
        BigDecimal discountAmount,
        BigDecimal maxDiscountAmount,
        BigDecimal minimumOrderPurchaseAmount,
        boolean isActive,
        String startDate,
        String endDate,
        List<Long> targetProductIds,
        List<Long> targetCategoryIds,
        List<Long> targetBrandIds,
        String stripeCouponId,
        String stripePromotionCodeId,
        String createdAt,
        String updatedAt
) {
}
