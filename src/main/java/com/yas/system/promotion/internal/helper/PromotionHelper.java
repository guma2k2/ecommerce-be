package com.yas.system.promotion.internal.helper;

import com.yas.system.catalog.api.dto.ProductVariantPublicDto;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.util.DateTimeUtils;
import com.yas.system.promotion.internal.constant.PromotionConstant;
import com.yas.system.promotion.internal.dto.request.CreatePromotionRequest;
import com.yas.system.promotion.internal.dto.request.UpdatePromotionRequest;
import com.yas.system.promotion.internal.dto.request.VerifyPromotionItemRequest;
import com.yas.system.promotion.internal.dto.response.PromotionDetailResponse;
import com.yas.system.promotion.internal.dto.response.PromotionResponse;
import com.yas.system.promotion.internal.dto.response.PromotionVerifyResponse;
import com.yas.system.promotion.internal.entity.Promotion;
import com.yas.system.promotion.internal.entity.PromotionApply;
import com.yas.system.promotion.internal.enumeration.ApplyTo;
import com.yas.system.promotion.internal.enumeration.DiscountType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
public class PromotionHelper {

    public Promotion toPromotion(CreatePromotionRequest request) {
        Promotion promotion = Promotion.builder()
                .name(request.name().trim())
                .slug(request.slug().trim())
                .description(request.description())
                .couponCode(request.couponCode().trim().toUpperCase())
                .discountType(request.discountType())
                .usageType(request.usageType())
                .applyTo(request.applyTo())
                .usageLimit(request.usageLimit())
                .usageLimitPerCustomer(request.usageLimitPerCustomer())
                .discountPercentage(request.discountPercentage())
                .discountAmount(request.discountAmount())
                .maxDiscountAmount(request.maxDiscountAmount())
                .minimumOrderPurchaseAmount(request.minimumOrderPurchaseAmount())
                .isActive(Boolean.TRUE.equals(request.isActive()))
                .startDate(request.startDate())
                .endDate(request.endDate())
                .applies(new ArrayList<>())
                .build();

        syncApplies(promotion, request.applyTo(), request.targetProductIds(), request.targetCategoryIds(), request.targetBrandIds());
        return promotion;
    }

    public void updatePromotion(Promotion promotion, UpdatePromotionRequest request) {
        promotion.setName(request.name().trim());
        promotion.setSlug(request.slug().trim());
        promotion.setDescription(request.description());
        promotion.setCouponCode(request.couponCode().trim().toUpperCase());
        promotion.setDiscountType(request.discountType());
        promotion.setUsageType(request.usageType());
        promotion.setApplyTo(request.applyTo());
        promotion.setUsageLimit(request.usageLimit());
        promotion.setUsageLimitPerCustomer(request.usageLimitPerCustomer());
        promotion.setDiscountPercentage(request.discountPercentage());
        promotion.setDiscountAmount(request.discountAmount());
        promotion.setMaxDiscountAmount(request.maxDiscountAmount());
        promotion.setMinimumOrderPurchaseAmount(request.minimumOrderPurchaseAmount());
        promotion.setActive(Boolean.TRUE.equals(request.isActive()));
        promotion.setStartDate(request.startDate());
        promotion.setEndDate(request.endDate());

        syncApplies(promotion, request.applyTo(), request.targetProductIds(), request.targetCategoryIds(), request.targetBrandIds());
    }

    public PromotionResponse toPromotionResponse(Promotion promotion) {
        return new PromotionResponse(
                promotion.getId(),
                promotion.getName(),
                promotion.getSlug(),
                promotion.getDescription(),
                promotion.getCouponCode(),
                promotion.getDiscountType(),
                promotion.getUsageType(),
                promotion.getApplyTo(),
                promotion.getUsageLimit(),
                promotion.getUsageCount(),
                promotion.getUsageLimitPerCustomer(),
                promotion.getDiscountPercentage(),
                promotion.getDiscountAmount(),
                promotion.getMaxDiscountAmount(),
                promotion.getMinimumOrderPurchaseAmount(),
                promotion.isActive(),
                DateTimeUtils.format(promotion.getStartDate()),
                DateTimeUtils.format(promotion.getEndDate()),
                DateTimeUtils.format(promotion.getCreatedAt()),
                DateTimeUtils.format(promotion.getUpdatedAt())
        );
    }

    public PromotionDetailResponse toPromotionDetailResponse(Promotion promotion) {
        List<Long> productIds = new ArrayList<>();
        List<Long> categoryIds = new ArrayList<>();
        List<Long> brandIds = new ArrayList<>();

        if (promotion.getApplies() != null) {
            for (PromotionApply apply : promotion.getApplies()) {
                if (apply.getProductId() != null) productIds.add(apply.getProductId());
                if (apply.getCategoryId() != null) categoryIds.add(apply.getCategoryId());
                if (apply.getBrandId() != null) brandIds.add(apply.getBrandId());
            }
        }

        return new PromotionDetailResponse(
                promotion.getId(),
                promotion.getName(),
                promotion.getSlug(),
                promotion.getDescription(),
                promotion.getCouponCode(),
                promotion.getDiscountType(),
                promotion.getUsageType(),
                promotion.getApplyTo(),
                promotion.getUsageLimit(),
                promotion.getUsageCount(),
                promotion.getUsageLimitPerCustomer(),
                promotion.getDiscountPercentage(),
                promotion.getDiscountAmount(),
                promotion.getMaxDiscountAmount(),
                promotion.getMinimumOrderPurchaseAmount(),
                promotion.isActive(),
                DateTimeUtils.format(promotion.getStartDate()),
                DateTimeUtils.format(promotion.getEndDate()),
                productIds,
                categoryIds,
                brandIds,
                DateTimeUtils.format(promotion.getCreatedAt()),
                DateTimeUtils.format(promotion.getUpdatedAt())
        );
    }

    public PromotionVerifyResponse calculateDiscount(
            Promotion promotion,
            BigDecimal orderAmount,
            List<VerifyPromotionItemRequest> items,
            Map<Long, ProductVariantPublicDto> variantMap
    ) {
        List<Long> eligibleVariantIds = new ArrayList<>();
        BigDecimal eligibleSubtotal = BigDecimal.ZERO;

        if (promotion.getApplyTo() == ApplyTo.ORDER) {
            eligibleSubtotal = orderAmount;
            eligibleVariantIds = items.stream().map(VerifyPromotionItemRequest::productVariantId).toList();
        } else {
            Set<Long> targetProductIds = new HashSet<>();
            Set<Long> targetCategoryIds = new HashSet<>();
            Set<Long> targetBrandIds = new HashSet<>();

            if (promotion.getApplies() != null) {
                for (PromotionApply apply : promotion.getApplies()) {
                    if (apply.getProductId() != null) targetProductIds.add(apply.getProductId());
                    if (apply.getCategoryId() != null) targetCategoryIds.add(apply.getCategoryId());
                    if (apply.getBrandId() != null) targetBrandIds.add(apply.getBrandId());
                }
            }

            for (VerifyPromotionItemRequest item : items) {
                ProductVariantPublicDto variant = variantMap.get(item.productVariantId());
                if (variant == null) {
                    continue;
                }

                boolean matches = false;
                if (promotion.getApplyTo() == ApplyTo.PRODUCT && variant.productId() != null && targetProductIds.contains(variant.productId())) {
                    matches = true;
                } else if (promotion.getApplyTo() == ApplyTo.CATEGORY && variant.categoryId() != null && targetCategoryIds.contains(variant.categoryId())) {
                    matches = true;
                } else if (promotion.getApplyTo() == ApplyTo.BRAND && variant.brandId() != null && targetBrandIds.contains(variant.brandId())) {
                    matches = true;
                }

                if (matches) {
                    eligibleVariantIds.add(item.productVariantId());
                    BigDecimal itemTotal = item.unitPrice().multiply(BigDecimal.valueOf(item.quantity()));
                    eligibleSubtotal = eligibleSubtotal.add(itemTotal);
                }
            }

            if (eligibleVariantIds.isEmpty()) {
                throw new ApplicationException(ErrorCode.PROMOTION_NOT_APPLICABLE);
            }
        }

        BigDecimal calculatedDiscount;
        BigDecimal discountValue;

        if (promotion.getDiscountType() == DiscountType.PERCENTAGE) {
            discountValue = promotion.getDiscountPercentage();
            calculatedDiscount = eligibleSubtotal.multiply(discountValue)
                    .divide(PromotionConstant.PERCENTAGE_DIVISOR, PromotionConstant.CURRENCY_SCALE, RoundingMode.HALF_UP);

            if (promotion.getMaxDiscountAmount() != null && promotion.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                calculatedDiscount = calculatedDiscount.min(promotion.getMaxDiscountAmount());
            }
        } else {
            discountValue = promotion.getDiscountAmount();
            calculatedDiscount = discountValue.min(eligibleSubtotal);
        }

        calculatedDiscount = calculatedDiscount.min(orderAmount);

        return new PromotionVerifyResponse(
                true,
                promotion.getCouponCode(),
                promotion.getId(),
                promotion.getName(),
                promotion.getDiscountType(),
                discountValue,
                calculatedDiscount,
                promotion.getApplyTo(),
                eligibleVariantIds
        );
    }

    private void syncApplies(Promotion promotion, ApplyTo applyTo, List<Long> productIds, List<Long> categoryIds, List<Long> brandIds) {
        promotion.getApplies().clear();
        if (applyTo == ApplyTo.ORDER) {
            return;
        }

        if (applyTo == ApplyTo.PRODUCT && productIds != null) {
            for (Long pid : productIds) {
                if (pid != null) {
                    promotion.getApplies().add(PromotionApply.builder().promotion(promotion).productId(pid).build());
                }
            }
        } else if (applyTo == ApplyTo.CATEGORY && categoryIds != null) {
            for (Long cid : categoryIds) {
                if (cid != null) {
                    promotion.getApplies().add(PromotionApply.builder().promotion(promotion).categoryId(cid).build());
                }
            }
        } else if (applyTo == ApplyTo.BRAND && brandIds != null) {
            for (Long bid : brandIds) {
                if (bid != null) {
                    promotion.getApplies().add(PromotionApply.builder().promotion(promotion).brandId(bid).build());
                }
            }
        }
    }
}
