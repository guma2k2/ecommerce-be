package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.ProductOption;
import com.yas.system.catalog.internal.entity.ProductVariant;
import com.yas.system.catalog.internal.entity.VariantOptionValue;
import com.yas.system.catalog.internal.entity.attribute.ProductAttributeValue;

import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String description,
        String slug,
        String metaTitle,
        String metaKeyword,
        String metaDescription,
        List<ProductAttributeValueResponse> attributes,
        List<ProductOptionResponse> options,
        List<ProductVariantResponse> variants
) {
    public static ProductResponse from(
            Product product,
            List<ProductAttributeValue> attributes,
            List<ProductOption> options,
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
                attributes.stream()
                        .map(ProductAttributeValueResponse::from)
                        .toList(),
                options.stream()
                        .map(ProductOptionResponse::from)
                        .toList(),
                variants.stream()
                        .map(variant -> ProductVariantResponse.from(variant, variantOptionValues))
                        .toList()
        );
    }
}
