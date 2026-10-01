package com.yas.system.order.internal.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productVariantId,
        String productName,
        String sku,
        String thumbnailUrl,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
}
