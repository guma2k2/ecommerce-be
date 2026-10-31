package com.yas.system.promotion.service;

import com.yas.system.common.response.PageResponse;
import com.yas.system.common.security.annotation.AuthUser;
import com.yas.system.promotion.api.dto.PromotionApplyResultDto;
import com.yas.system.promotion.api.dto.PromotionOrderItemDto;
import com.yas.system.promotion.internal.dto.request.CreatePromotionRequest;
import com.yas.system.promotion.internal.dto.request.UpdatePromotionRequest;
import com.yas.system.promotion.internal.dto.request.VerifyPromotionRequest;
import com.yas.system.promotion.internal.dto.response.PromotionDetailResponse;
import com.yas.system.promotion.internal.dto.response.PromotionResponse;
import com.yas.system.promotion.internal.dto.response.PromotionVerifyResponse;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface PromotionService {

    PromotionVerifyResponse verifyPromotion(AuthUser customer, VerifyPromotionRequest request);

    PageResponse<PromotionResponse> getPromotions(
            int pageNumber,
            int pageSize,
            String name,
            String couponCode,
            Boolean isActive,
            ZonedDateTime startDate,
            ZonedDateTime endDate
    );

    PromotionDetailResponse getPromotionById(Long id);

    PromotionDetailResponse createPromotion(CreatePromotionRequest request);

    PromotionDetailResponse updatePromotion(Long id, UpdatePromotionRequest request);

    void deletePromotion(Long id);

    PromotionApplyResultDto applyPromotion(
            UUID orderId,
            String customerId,
            String couponCode,
            BigDecimal orderAmount,
            List<PromotionOrderItemDto> items
    );

    void revertPromotionUsage(UUID orderId);
}
