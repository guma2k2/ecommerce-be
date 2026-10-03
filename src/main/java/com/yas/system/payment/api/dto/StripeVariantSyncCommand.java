package com.yas.system.payment.api.dto;

import java.math.BigDecimal;

public record StripeVariantSyncCommand(
        Long productId,
        Long variantId,
        String name,
        String description,
        String sku,
        BigDecimal price,
        String currency,
        String imageUrl,
        String existingStripeProductId,
        String existingStripePriceId,
        boolean active
) {
}
