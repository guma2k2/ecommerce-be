package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.attribute.ProductAttribute;
import java.time.format.DateTimeFormatter;

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
                productAttribute.getCreatedAt() != null ? productAttribute.getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null,
                productAttribute.getUpdatedAt() != null ? productAttribute.getUpdatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null
        );
    }
}
