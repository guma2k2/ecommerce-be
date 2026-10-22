package com.yas.system.promotion.internal.dto.response;

import com.yas.system.promotion.internal.enumeration.ApplyTo;
import com.yas.system.promotion.internal.enumeration.DiscountType;
import com.yas.system.promotion.internal.enumeration.UsageType;

import java.math.BigDecimal;

public record PromotionResponse(
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
        String createdAt,
        String updatedAt
) {
}
