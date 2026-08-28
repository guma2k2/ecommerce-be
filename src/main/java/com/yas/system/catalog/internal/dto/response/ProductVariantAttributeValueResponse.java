package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.attribute.ProductVariantAttributeValue;

public record ProductVariantAttributeValueResponse(
        Long productAttributeId,
        String name,
        String value
) {
    public static ProductVariantAttributeValueResponse from(ProductVariantAttributeValue attributeValue) {
        return new ProductVariantAttributeValueResponse(
                attributeValue.getProductAttribute().getId(),
                attributeValue.getProductAttribute().getName(),
                attributeValue.getValue()
        );
    }
}
