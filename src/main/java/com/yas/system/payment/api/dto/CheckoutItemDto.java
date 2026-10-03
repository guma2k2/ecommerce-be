package com.yas.system.payment.api.dto;

import java.math.BigDecimal;

public record CheckoutItemDto(
        String stripePriceId,
        String productName,
        BigDecimal unitPrice,
        int quantity,
        String thumbnailUrl
) {
}
