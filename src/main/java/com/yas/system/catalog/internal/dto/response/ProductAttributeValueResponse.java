package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.attribute.ProductAttributeValue;

public record ProductAttributeValueResponse (
        Long productAttributeId,
        String name,
        String value
) {
    public static ProductAttributeValueResponse from(ProductAttributeValue attributeValue) {
        return new ProductAttributeValueResponse(
                attributeValue.getProductAttribute().getId(),
                attributeValue.getProductAttribute().getName(),
                attributeValue.getValue()
        );
    }
}
