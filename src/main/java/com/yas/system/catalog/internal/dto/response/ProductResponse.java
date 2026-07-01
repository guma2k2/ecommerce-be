package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.ProductOption;
import com.yas.system.catalog.internal.entity.ProductVariant;
import com.yas.system.catalog.internal.entity.VariantOptionValue;

import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String description,
        String slug,
        ProductOptionResponse[] options,
        ProductVariantResponse[] variants
) {
    public static ProductResponse from(
            Product product,
            List<ProductOption> options,
            List<ProductVariant> variants,
            List<VariantOptionValue> variantOptionValues
    ) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSlug(),
                options.stream()
                        .map(ProductOptionResponse::from)
                        .toArray(ProductOptionResponse[]::new),
                variants.stream()
                        .map(variant -> ProductVariantResponse.from(variant, variantOptionValues))
                        .toArray(ProductVariantResponse[]::new)
        );
    }
}
