package com.yas.system.catalog.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductVariantPublicDto(
        Long variantId,
        Long productId,
        String productName,
        String productSlug,
        String thumbnailUrl,
        Integer stockQuantity,
        String sku,
        BigDecimal price,
        List<ProductOptionPublicDto> options,
        String stripeProductId,
        String stripePriceId
) {
}
