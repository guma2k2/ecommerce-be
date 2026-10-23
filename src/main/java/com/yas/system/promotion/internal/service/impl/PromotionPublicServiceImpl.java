package com.yas.system.promotion.internal.service.impl;

import com.yas.system.catalog.api.CatalogPublicService;
import com.yas.system.catalog.api.dto.ProductVariantPublicDto;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.promotion.api.PromotionPublicService;
import com.yas.system.promotion.api.dto.PromotionApplyResultDto;
import com.yas.system.promotion.api.dto.PromotionOrderItemDto;
import com.yas.system.promotion.internal.dto.request.VerifyPromotionItemRequest;
import com.yas.system.promotion.internal.dto.response.PromotionVerifyResponse;
import com.yas.system.promotion.internal.entity.Promotion;
import com.yas.system.promotion.internal.entity.PromotionUsage;
import com.yas.system.promotion.internal.enumeration.ApplyTo;
import com.yas.system.promotion.internal.enumeration.PromotionUsageStatus;
import com.yas.system.promotion.internal.enumeration.UsageType;
import com.yas.system.promotion.internal.helper.PromotionHelper;
import com.yas.system.promotion.internal.repository.PromotionRepository;
import com.yas.system.promotion.internal.repository.PromotionUsageRepository;
import com.yas.system.promotion.internal.validator.PromotionValidator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromotionPublicServiceImpl implements PromotionPublicService {

    PromotionRepository promotionRepository;
    PromotionUsageRepository promotionUsageRepository;
    PromotionValidator promotionValidator;
    PromotionHelper promotionHelper;
    CatalogPublicService catalogPublicService;

    @Override
    @Transactional
    public PromotionApplyResultDto applyPromotion(
            UUID orderId,
            String customerId,
            String couponCode,
            BigDecimal orderAmount,
            List<PromotionOrderItemDto> items
    ) {
        if (couponCode == null || couponCode.isBlank()) {
            throw new ApplicationException(ErrorCode.PROMOTION_NOT_FOUND);
        }

        String normalizedCode = couponCode.trim();
        Promotion promotion = promotionRepository.findByCouponCodeIgnoreCaseWithApplies(normalizedCode)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PROMOTION_NOT_FOUND));

        promotionValidator.validateEligibility(promotion, orderAmount, customerId);

        List<VerifyPromotionItemRequest> verifyItems = items.stream()
                .map(item -> new VerifyPromotionItemRequest(item.productVariantId(), item.quantity(), item.unitPrice()))
                .toList();

        Map<Long, ProductVariantPublicDto> variantMap = Map.of();
        if (promotion.getApplyTo() != ApplyTo.ORDER) {
            List<Long> variantIds = verifyItems.stream()
                    .map(VerifyPromotionItemRequest::productVariantId)
                    .distinct()
                    .toList();
            variantMap = catalogPublicService.getProductVariantsByIds(variantIds);
        }

        PromotionVerifyResponse verifyResponse = promotionHelper.calculateDiscount(promotion, orderAmount, verifyItems, variantMap);

        // Atomic quota increment to prevent overselling under high concurrency
        int rowsUpdated = promotionRepository.incrementUsageCountAtomic(promotion.getId(), UsageType.UNLIMITED);
        if (rowsUpdated == 0) {
            throw new ApplicationException(ErrorCode.PROMOTION_USAGE_EXHAUSTED);
        }

        PromotionUsage usage = PromotionUsage.builder()
                .promotion(promotion)
                .customerId(customerId)
                .orderId(orderId)
                .discountAmount(verifyResponse.discountAmount())
                .status(PromotionUsageStatus.APPLIED)
                .build();
        promotionUsageRepository.save(usage);

        log.info("Successfully applied promotion {} to order {} with discount {}",
                promotion.getCouponCode(), orderId, verifyResponse.discountAmount());

        return new PromotionApplyResultDto(
                promotion.getId(),
                promotion.getCouponCode(),
                verifyResponse.discountAmount(),
                promotion.getStripeCouponId(),
                promotion.getStripePromotionCodeId()
        );
    }
}
