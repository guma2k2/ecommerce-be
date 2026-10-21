package com.yas.system.promotion.api;

import com.yas.system.promotion.api.dto.PromotionApplyResultDto;
import com.yas.system.promotion.api.dto.PromotionOrderItemDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PromotionPublicService {

    PromotionApplyResultDto applyPromotion(
            UUID orderId,
            String customerId,
            String couponCode,
            BigDecimal orderAmount,
            List<PromotionOrderItemDto> items
    );
}
