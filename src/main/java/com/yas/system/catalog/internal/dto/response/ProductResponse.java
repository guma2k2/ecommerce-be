package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.ProductMedia;
import com.yas.system.catalog.internal.entity.attribute.ProductAttributeValue;
import com.yas.system.catalog.internal.entity.attribute.ProductVariantAttributeValue;
import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import com.yas.system.catalog.internal.entity.variant.VariantOptionValue;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public record ProductResponse(
        Long id,
        String name,
        String description,
        String slug,
        String metaTitle,
        String metaKeyword,
        String metaDescription,
        BrandResponse brand,
        List<ProductMediaResponse> medias,
        List<ProductAttributeValueResponse> attributes,
        List<ProductOptionCombinationResponse> options,
        List<ProductVariantResponse> variants,
        String createdAt,
        String updatedAt
) {
    public static ProductResponse from(
            Product product,
            List<ProductMedia> medias,
            Map<String, String> mediaUrlMap,
            List<ProductAttributeValue> attributes,
            List<ProductOptionCombinationResponse> options,
            List<ProductVariant> variants,
            List<VariantOptionValue> variantOptionValues,
            List<ProductVariantAttributeValue> variantAttributeValues
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
                medias != null
                        ? medias.stream()
                                .map(pm -> ProductMediaResponse.from(pm, mediaUrlMap != null ? mediaUrlMap.get(pm.getMediaId()) : null))
                                .toList()
                        : List.of(),
                attributes.stream()
                        .map(ProductAttributeValueResponse::from)
                        .toList(),
                options,
                variants.stream()
                        .map(variant -> ProductVariantResponse.from(variant, variantOptionValues, variantAttributeValues))
                        .toList(),
                product.getCreatedAt() != null ? product.getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null,
                product.getUpdatedAt() != null ? product.getUpdatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null
        );
    }

    public static ProductResponse from(
            Product product,
            List<ProductMedia> medias,
            List<ProductAttributeValue> attributes,
            List<ProductOptionCombinationResponse> options,
            List<ProductVariant> variants,
            List<VariantOptionValue> variantOptionValues,
            List<ProductVariantAttributeValue> variantAttributeValues
    ) {
        return from(product, medias, java.util.Map.of(), attributes, options, variants, variantOptionValues, variantAttributeValues);
    }
}

