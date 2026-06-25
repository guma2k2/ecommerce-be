package com.yas.system.catalog.internal.dto.response;

import com.tiki.product.entity.Product;

import java.util.List;
import java.util.Map;

public record ProductResponse(
        Long id,
        String name,
        String slug,
        String description,
        BrandResponse brand,
        CategoryResponse category,
        List<ProductVariantResponse> productVariants,
        List<ProductImageResponse> productImages,
        Map<String, String> productAttribute
) {
    public static ProductResponse from(Product product, BrandResponse brand, CategoryResponse category, List<ProductVariantResponse> productVariants, List<ProductImageResponse> productImages, Map<String, String> productAttribute) {
        return new ProductResponse(product.getId(), product.getName(), product.getSlug(), product.getDescription(), brand, category, productVariants, productImages, productAttribute);
    }
}
