package com.yas.system.catalog.api.dto;

import com.yas.system.catalog.api.enumeration.ProductStatus;

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
        String status,
        List<ProductOptionPublicDto> options,
        String stripeProductId,
        String stripePriceId
) {
    public boolean isProductActive() {
        return ProductStatus.ACTIVE.name().equals(status);
    }
}
