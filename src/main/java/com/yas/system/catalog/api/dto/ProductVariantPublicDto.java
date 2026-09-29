package com.yas.system.catalog.api.dto;

import java.math.BigDecimal;

public record ProductVariantPublicDto(
        Long variantId,
        Long productId,
        String productName,
        String productSlug,
        String thumbnailUrl,
        Integer stockQuantity,
        String sku,
        BigDecimal price,
        String status
) {
}
