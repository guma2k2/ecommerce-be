package com.yas.system.promotion.api.dto;

import java.math.BigDecimal;

public record PromotionOrderItemDto(
        Long productVariantId,
        int quantity,
        BigDecimal unitPrice
) {
}
