package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.attribute.ProductAttribute;
import com.yas.system.common.util.DateTimeUtils;

public record ProductAttributeResponse (
        Long id,
        String name,
        String createdAt,
        String updatedAt
) {
    public static ProductAttributeResponse from(ProductAttribute productAttribute) {
        return new ProductAttributeResponse(
                productAttribute.getId(),
                productAttribute.getName(),
                DateTimeUtils.format(productAttribute.getCreatedAt()),
                DateTimeUtils.format(productAttribute.getUpdatedAt())
        );
    }
}
