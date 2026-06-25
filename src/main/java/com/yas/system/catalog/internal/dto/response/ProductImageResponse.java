package com.yas.system.catalog.internal.dto.response;

import com.tiki.product.entity.ProductImage;

public record ProductImageResponse (
        Long id,
        String url,
        String type,
        int sortOrder,
        boolean status,
        Long productId,
        Long productVariantId
) {
    public static ProductImageResponse from(ProductImage productImage) {
        return new ProductImageResponse(productImage.getId(), productImage.getUrl(), productImage.getType().name(), productImage.getSortOrder(), productImage.isStatus(), productImage.getProduct().getId(), productImage.getProductVariant().getId());
    }
}
