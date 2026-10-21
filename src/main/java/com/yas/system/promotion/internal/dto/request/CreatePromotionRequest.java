package com.yas.system.promotion.internal.dto.request;

import com.yas.system.promotion.internal.enumeration.ApplyTo;
import com.yas.system.promotion.internal.enumeration.DiscountType;
import com.yas.system.promotion.internal.enumeration.UsageType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public record CreatePromotionRequest(
        @NotBlank(message = "Promotion name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @NotBlank(message = "Slug is required")
        @Size(max = 100, message = "Slug must not exceed 100 characters")
        String slug,

        String description,

        @NotBlank(message = "Coupon code is required")
        @Size(max = 50, message = "Coupon code must not exceed 50 characters")
        String couponCode,

        @NotNull(message = "Discount type is required")
        DiscountType discountType,

        @NotNull(message = "Usage type is required")
        UsageType usageType,

        @NotNull(message = "ApplyTo target is required")
        ApplyTo applyTo,

        Integer usageLimit,

        Integer usageLimitPerCustomer,

        @DecimalMin(value = "0.0", message = "Discount percentage cannot be negative")
        BigDecimal discountPercentage,

        @DecimalMin(value = "0.0", message = "Discount amount cannot be negative")
        BigDecimal discountAmount,

        BigDecimal maxDiscountAmount,

        BigDecimal minimumOrderPurchaseAmount,

        Boolean isActive,

        @NotNull(message = "Start date is required")
        ZonedDateTime startDate,

        @NotNull(message = "End date is required")
        ZonedDateTime endDate,

        List<Long> targetProductIds,
        List<Long> targetCategoryIds,
        List<Long> targetBrandIds
) {
        public CreatePromotionRequest {
                if (targetProductIds == null) targetProductIds = List.of();
                if (targetCategoryIds == null) targetCategoryIds = List.of();
                if (targetBrandIds == null) targetBrandIds = List.of();
                if (isActive == null) isActive = true;
                if (usageLimitPerCustomer == null) usageLimitPerCustomer = 1;
        }
}
