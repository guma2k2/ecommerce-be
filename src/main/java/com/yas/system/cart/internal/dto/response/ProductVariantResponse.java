package com.yas.system.cart.internal.dto.response;

import java.math.BigDecimal;

public record ProductVariantResponse(
        Long variantId,
        Long productId,
        String productName,
        String productSlug,
        String thumbnailUrl,
        int stockQuantity,
        String sku,
        BigDecimal price
) {
}
