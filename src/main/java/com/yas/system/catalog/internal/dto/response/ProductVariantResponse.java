package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.attribute.ProductVariantAttributeValue;
import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import com.yas.system.catalog.internal.entity.variant.VariantOptionValue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record ProductVariantResponse(
        Long id,
        String title,
        List<Long> productOptionValueIds,
        List<ProductVariantAttributeValueResponse> attributeValues,
        String sku,
        BigDecimal price,
        int quantity
) {
    public static ProductVariantResponse from(
            ProductVariant variant,
            List<VariantOptionValue> optionValues,
            List<ProductVariantAttributeValue> variantAttributeValues
    ) {
        List<Long> productOptionValueIds = Objects.isNull(optionValues) ? List.of() : optionValues.stream()
                .filter(optionValue -> Objects.nonNull(optionValue.getProductVariant())
                        && Objects.equals(optionValue.getProductVariant().getId(), variant.getId()))
                .filter(optionValue -> Objects.nonNull(optionValue.getProductOptionValue()))
                .map(optionValue -> optionValue.getProductOptionValue().getId())
                .toList();

        List<ProductVariantAttributeValueResponse> attributeValueResponses = Objects.isNull(variantAttributeValues) ? List.of() : variantAttributeValues.stream()
                .filter(attrValue -> Objects.nonNull(attrValue.getProductVariant())
                        && Objects.equals(attrValue.getProductVariant().getId(), variant.getId()))
                .map(ProductVariantAttributeValueResponse::from)
                .toList();

        return new ProductVariantResponse(
                variant.getId(),
                variant.getTitle(),
                productOptionValueIds,
                attributeValueResponses,
                variant.getSku(),
                variant.getPrice(),
                Objects.nonNull(variant.getQuantity()) ? variant.getQuantity() : 0
        );
    }
}
