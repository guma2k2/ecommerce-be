package com.yas.system.promotion.internal.validator;

import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.promotion.internal.dto.request.CreatePromotionRequest;
import com.yas.system.promotion.internal.dto.request.UpdatePromotionRequest;
import com.yas.system.promotion.internal.entity.Promotion;
import com.yas.system.promotion.internal.enumeration.ApplyTo;
import com.yas.system.promotion.internal.enumeration.DiscountType;
import com.yas.system.promotion.internal.enumeration.PromotionUsageStatus;
import com.yas.system.promotion.internal.enumeration.UsageType;
import com.yas.system.promotion.internal.repository.PromotionRepository;
import com.yas.system.promotion.internal.repository.PromotionUsageRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromotionValidator {

    PromotionRepository promotionRepository;
    PromotionUsageRepository promotionUsageRepository;

    public void validateCreate(CreatePromotionRequest request) {
        validateDateRange(request.startDate(), request.endDate());
        validateDiscountValues(request.discountType(), request.discountPercentage(), request.discountAmount());
        validateUsageLimit(request.usageType(), request.usageLimit());
        validateTargets(request.applyTo(), request.targetProductIds(), request.targetCategoryIds(), request.targetBrandIds());

        if (promotionRepository.existsByCouponCodeIgnoreCase(request.couponCode().trim())) {
            throw new ApplicationException(ErrorCode.COUPON_CODE_ALREADY_EXISTS, request.couponCode().trim());
        }

        if (promotionRepository.existsBySlug(request.slug().trim())) {
            throw new ApplicationException(ErrorCode.PROMOTION_SLUG_ALREADY_EXISTS, request.slug().trim());
        }
    }

    public void validateUpdate(Long id, UpdatePromotionRequest request) {
        validateDateRange(request.startDate(), request.endDate());
        validateDiscountValues(request.discountType(), request.discountPercentage(), request.discountAmount());
        validateUsageLimit(request.usageType(), request.usageLimit());
        validateTargets(request.applyTo(), request.targetProductIds(), request.targetCategoryIds(), request.targetBrandIds());

        if (promotionRepository.existsByCouponCodeIgnoreCaseAndIdNot(request.couponCode().trim(), id)) {
            throw new ApplicationException(ErrorCode.COUPON_CODE_ALREADY_EXISTS, request.couponCode().trim());
        }

        if (promotionRepository.existsBySlugAndIdNot(request.slug().trim(), id)) {
            throw new ApplicationException(ErrorCode.PROMOTION_SLUG_ALREADY_EXISTS, request.slug().trim());
        }
    }

    public void validateEligibility(Promotion promotion, BigDecimal orderAmount, String customerId) {
        if (!promotion.isActive()) {
            throw new ApplicationException(ErrorCode.PROMOTION_NOT_FOUND);
        }

        ZonedDateTime now = ZonedDateTime.now();
        if (now.isBefore(promotion.getStartDate()) || now.isAfter(promotion.getEndDate())) {
            throw new ApplicationException(ErrorCode.PROMOTION_EXPIRED);
        }

        if (promotion.getUsageType() == UsageType.LIMITED && promotion.getUsageCount() >= promotion.getUsageLimit()) {
            throw new ApplicationException(ErrorCode.PROMOTION_USAGE_EXHAUSTED);
        }

        if (promotion.getMinimumOrderPurchaseAmount() != null
                && orderAmount.compareTo(promotion.getMinimumOrderPurchaseAmount()) < 0) {
            throw new ApplicationException(ErrorCode.MINIMUM_PURCHASE_NOT_MET, promotion.getMinimumOrderPurchaseAmount());
        }

        if (customerId != null && promotion.getUsageLimitPerCustomer() != null && promotion.getUsageLimitPerCustomer() > 0) {
            long usedCount = promotionUsageRepository.countByPromotionIdAndCustomerIdAndStatus(
                    promotion.getId(),
                    customerId,
                    PromotionUsageStatus.APPLIED
            );
            if (usedCount >= promotion.getUsageLimitPerCustomer()) {
                throw new ApplicationException(ErrorCode.CUSTOMER_USAGE_LIMIT_EXCEEDED);
            }
        }
    }

    private void validateDateRange(ZonedDateTime startDate, ZonedDateTime endDate) {
        if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
            throw new ApplicationException(ErrorCode.BAD_REQUEST);
        }
    }

    private void validateDiscountValues(DiscountType type, BigDecimal percentage, BigDecimal amount) {
        if (type == DiscountType.PERCENTAGE) {
            if (percentage == null || percentage.compareTo(BigDecimal.ZERO) <= 0 || percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new ApplicationException(ErrorCode.BAD_REQUEST);
            }
        } else if (type == DiscountType.FIXED) {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ApplicationException(ErrorCode.BAD_REQUEST);
            }
        }
    }

    private void validateUsageLimit(UsageType usageType, Integer usageLimit) {
        if (usageType == UsageType.LIMITED && (usageLimit == null || usageLimit <= 0)) {
            throw new ApplicationException(ErrorCode.BAD_REQUEST);
        }
    }

    private void validateTargets(ApplyTo applyTo, List<Long> products, List<Long> categories, List<Long> brands) {
        if (applyTo == ApplyTo.PRODUCT && (products == null || products.isEmpty())) {
            throw new ApplicationException(ErrorCode.BAD_REQUEST);
        }
        if (applyTo == ApplyTo.CATEGORY && (categories == null || categories.isEmpty())) {
            throw new ApplicationException(ErrorCode.BAD_REQUEST);
        }
        if (applyTo == ApplyTo.BRAND && (brands == null || brands.isEmpty())) {
            throw new ApplicationException(ErrorCode.BAD_REQUEST);
        }
    }
}
