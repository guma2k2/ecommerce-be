package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import com.yas.system.catalog.internal.entity.variant.VariantOptionValue;
import com.yas.system.catalog.internal.entity.attribute.ProductAttributeValue;

import java.time.format.DateTimeFormatter;
import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String description,
        String slug,
        String metaTitle,
        String metaKeyword,
        String metaDescription,
        BrandResponse brand,
        List<ProductAttributeValueResponse> attributes,
        List<ProductOptionCombinationResponse> options,
        List<ProductVariantResponse> variants,
        String createdAt,
        String updatedAt
) {
    public static ProductResponse from(
            Product product,
            List<ProductAttributeValue> attributes,
            List<ProductOptionCombinationResponse> options,
            List<ProductVariant> variants,
            List<VariantOptionValue> variantOptionValues
    ) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSlug(),
                product.getMetaTitle(),
                product.getMetaKeyword(),
                product.getMetaDescription(),
                product.getBrand() != null ? BrandResponse.from(product.getBrand()) : null,
                attributes.stream()
                        .map(ProductAttributeValueResponse::from)
                        .toList(),
                options,
                variants.stream()
                        .map(variant -> ProductVariantResponse.from(variant, variantOptionValues))
                        .toList(),
                product.getCreatedAt() != null ? product.getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null,
                product.getUpdatedAt() != null ? product.getUpdatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null
        );
    }
}
