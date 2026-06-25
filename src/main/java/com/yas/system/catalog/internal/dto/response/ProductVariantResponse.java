package com.yas.system.catalog.internal.dto.response;

import com.tiki.product.entity.Product;
import com.tiki.product.entity.ProductVariant;

import java.util.List;
import java.util.Map;

public record ProductVariantResponse(
        Long  id,
        String sku,
        Double price,
        Integer quantity,
        String status,
        Map<String, String> variantAttributeValue,
        String name,
        String slug,
        String description,
        Float rating,
        Integer reviewsCount,
        String imageUrl,
        String category,
        String brand

) {
    public static ProductVariantResponse from(
            ProductVariant productVariant,
            Map<String, String> variantAttributeValue,
            Product product,
            String imageUrl,
            Float rating,
            Integer reviewsCount
    ) {
        String category= product.getCategory() != null ? product.getCategory().getName() : null;
        String brand= product.getBrand() != null ? product.getBrand().getName() : null;
        return new ProductVariantResponse(
                productVariant.getId(),
                productVariant.getSku(),
                productVariant.getPrice(),
                productVariant.getQuantity(),
                productVariant.getStatus(),
                variantAttributeValue,
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                rating,
                reviewsCount,
                imageUrl,
                category,
                brand
        );
    }

    public static ProductVariantResponse from(ProductVariant productVariant, Map<String, String> variantAttributeValue) {
        return new ProductVariantResponse(productVariant.getId(),
                productVariant.getSku(),
                productVariant.getPrice(),
                productVariant.getQuantity(),
                productVariant.getStatus(),
                variantAttributeValue, null, null, null, null, null, null, null, null);
    }





}
