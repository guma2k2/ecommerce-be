package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.ProductMedia;
import com.yas.system.catalog.internal.entity.attribute.ProductAttributeValue;
import com.yas.system.catalog.internal.entity.attribute.ProductVariantAttributeValue;
import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import com.yas.system.catalog.internal.entity.variant.VariantOptionValue;

import com.yas.system.common.util.DateTimeUtils;
import com.yas.system.media.api.dto.MediaPublicDto;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record ProductResponse(
        Long id,
        String name,
        String description,
        String slug,
        String metaTitle,
        String metaKeyword,
        String metaDescription,
        String status,
        BrandResponse brand,
        CategoryResponse category,
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
            Map<String, MediaPublicDto> mediaMap,
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
                product.getStatus().name(),
                product.getBrand() != null ? BrandResponse.from(product.getBrand()) : null,
                product.getCategory() != null ? CategoryResponse.fromWithoutChildren(product.getCategory()) : null,
                medias != null
                        ? medias.stream()
                                .map(pm -> {
                                    List<Long> linkedVariantIds = variants != null
                                            ? variants.stream()
                                                    .filter(v -> Objects.nonNull(v.getMediaId()) && Objects.equals(v.getMediaId(), pm.getMediaId()))
                                                    .map(ProductVariant::getId)
                                                    .filter(Objects::nonNull)
                                                    .toList()
                                            : List.of();
                                    return ProductMediaResponse.from(
                                            pm,
                                            mediaMap != null ? mediaMap.get(pm.getMediaId()) : null,
                                            linkedVariantIds
                                    );
                                })
                                .toList()
                        : List.of(),
                attributes.stream()
                        .map(ProductAttributeValueResponse::from)
                        .toList(),
                options,
                variants != null
                        ? variants.stream()
                                .map(variant -> ProductVariantResponse.from(
                                        variant,
                                        variantOptionValues,
                                        variantAttributeValues,
                                        mediaMap != null && variant.getMediaId() != null && mediaMap.get(variant.getMediaId()) != null
                                                ? mediaMap.get(variant.getMediaId()).url()
                                                : null
                                ))
                                .toList()
                        : List.of(),
                DateTimeUtils.format(product.getCreatedAt()),
                DateTimeUtils.format(product.getUpdatedAt())
        );
    }
}


