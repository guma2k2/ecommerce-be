package com.yas.system.promotion.internal.service.impl;

import com.yas.system.catalog.api.CatalogPublicService;
import com.yas.system.catalog.api.dto.ProductVariantPublicDto;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.response.PageResponse;
import com.yas.system.common.security.annotation.AuthUser;
import com.yas.system.promotion.internal.constant.PromotionConstant;
import com.yas.system.promotion.internal.dto.request.CreatePromotionRequest;
import com.yas.system.promotion.internal.dto.request.UpdatePromotionRequest;
import com.yas.system.promotion.internal.dto.request.VerifyPromotionItemRequest;
import com.yas.system.promotion.internal.dto.request.VerifyPromotionRequest;
import com.yas.system.promotion.internal.dto.response.PromotionDetailResponse;
import com.yas.system.promotion.internal.dto.response.PromotionResponse;
import com.yas.system.promotion.internal.dto.response.PromotionVerifyResponse;
import com.yas.system.promotion.internal.entity.Promotion;
import com.yas.system.promotion.internal.entity.PromotionUsage;
import com.yas.system.promotion.internal.enumeration.ApplyTo;
import com.yas.system.promotion.internal.enumeration.PromotionUsageStatus;
import com.yas.system.promotion.internal.helper.PromotionHelper;
import com.yas.system.promotion.internal.repository.PromotionRepository;
import com.yas.system.promotion.internal.repository.PromotionUsageRepository;
import com.yas.system.promotion.internal.service.PromotionService;
import com.yas.system.promotion.internal.specification.PromotionSpecification;
import com.yas.system.promotion.events.PromotionSyncEvent;
import com.yas.system.promotion.events.SyncAction;
import com.yas.system.promotion.internal.validator.PromotionValidator;
import org.springframework.context.ApplicationEventPublisher;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromotionServiceImpl implements PromotionService {

    PromotionRepository promotionRepository;
    PromotionUsageRepository promotionUsageRepository;
    PromotionValidator promotionValidator;
    PromotionHelper promotionHelper;
    CatalogPublicService catalogPublicService;
    ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public PromotionVerifyResponse verifyPromotion(AuthUser customer, VerifyPromotionRequest request) {
        String couponCode = request.couponCode().trim();
        Promotion promotion = promotionRepository.findByCouponCodeIgnoreCaseWithApplies(couponCode)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PROMOTION_NOT_FOUND));

        String customerId = customer != null ? customer.id() : null;
        promotionValidator.validateEligibility(promotion, request.orderAmount(), customerId);

        Map<Long, ProductVariantPublicDto> variantMap = Map.of();
        if (promotion.getApplyTo() != ApplyTo.ORDER) {
            List<Long> variantIds = request.items().stream()
                    .map(VerifyPromotionItemRequest::productVariantId)
                    .distinct()
                    .toList();
            variantMap = catalogPublicService.getProductVariantsByIds(variantIds);
        }

        return promotionHelper.calculateDiscount(promotion, request.orderAmount(), request.items(), variantMap);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PromotionResponse> getPromotions(
            int pageNumber,
            int pageSize,
            String name,
            String couponCode,
            Boolean isActive,
            ZonedDateTime startDate,
            ZonedDateTime endDate
    ) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, PromotionConstant.DEFAULT_SORT_BY));
        Page<Promotion> page = promotionRepository.findAll(
                PromotionSpecification.filter(name, couponCode, isActive, startDate, endDate),
                pageable
        );

        List<PromotionResponse> content = page.getContent().stream()
                .map(promotionHelper::toPromotionResponse)
                .toList();

        return new PageResponse<>(
                page.getNumber(),
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                content
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionDetailResponse getPromotionById(Long id) {
        Promotion promotion = promotionRepository.findByIdWithApplies(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PROMOTION_NOT_FOUND));
        return promotionHelper.toPromotionDetailResponse(promotion);
    }

    @Override
    @Transactional
    public PromotionDetailResponse createPromotion(CreatePromotionRequest request) {
        promotionValidator.validateCreate(request);
        Promotion promotion = promotionHelper.toPromotion(request);
        Promotion savedPromotion = promotionRepository.save(promotion);
        log.info("Created promotion id: {}, couponCode: {}", savedPromotion.getId(), savedPromotion.getCouponCode());
        eventPublisher.publishEvent(new PromotionSyncEvent(savedPromotion.getId(), SyncAction.UPSERT));
        return promotionHelper.toPromotionDetailResponse(savedPromotion);
    }

    @Override
    @Transactional
    public PromotionDetailResponse updatePromotion(Long id, UpdatePromotionRequest request) {
        promotionValidator.validateUpdate(id, request);
        Promotion promotion = promotionRepository.findByIdWithApplies(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PROMOTION_NOT_FOUND));

        promotionHelper.updatePromotion(promotion, request);
        Promotion updatedPromotion = promotionRepository.save(promotion);
        log.info("Updated promotion id: {}, couponCode: {}", updatedPromotion.getId(), updatedPromotion.getCouponCode());
        eventPublisher.publishEvent(new PromotionSyncEvent(updatedPromotion.getId(), SyncAction.UPSERT));
        return promotionHelper.toPromotionDetailResponse(updatedPromotion);
    }

    @Override
    @Transactional
    public void deletePromotion(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PROMOTION_NOT_FOUND));

        if (promotionUsageRepository.existsByPromotionId(id)) {
            throw new ApplicationException(ErrorCode.PROMOTION_IN_USE);
        }

        String stripeCouponId = promotion.getStripeCouponId();
        String stripePromotionCodeId = promotion.getStripePromotionCodeId();
        promotionRepository.delete(promotion);
        log.info("Deleted promotion id: {}", id);
        eventPublisher.publishEvent(new PromotionSyncEvent(id, SyncAction.DELETE, stripeCouponId, stripePromotionCodeId));
    }

    @Override
    @Transactional
    public void revertPromotionUsage(UUID orderId) {
        promotionUsageRepository.findByOrderIdAndStatus(orderId, PromotionUsageStatus.APPLIED)
                .ifPresent(usage -> {
                    usage.setStatus(PromotionUsageStatus.REVERTED);
                    promotionUsageRepository.save(usage);
                    promotionRepository.decrementUsageCountAtomic(usage.getPromotion().getId());
                    log.info("Reverted promotion usage for orderId: {}, promotionId: {}", orderId, usage.getPromotion().getId());
                });
    }
}
