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
        String status,
        List<ProductOptionPublicDto> options,
        String stripeProductId,
        String stripePriceId
) {
    public ProductVariantPublicDto(
            Long variantId,
            Long productId,
            String productName,
            String productSlug,
            String thumbnailUrl,
            Integer stockQuantity,
            String sku,
            BigDecimal price,
            String status,
            List<ProductOptionPublicDto> options
    ) {
        this(variantId, productId, productName, productSlug, thumbnailUrl, stockQuantity, sku, price, status, options, null, null);
    }

    public ProductVariantPublicDto(
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
        this(variantId, productId, productName, productSlug, thumbnailUrl, stockQuantity, sku, price, status, List.of(), null, null);
    }
}
