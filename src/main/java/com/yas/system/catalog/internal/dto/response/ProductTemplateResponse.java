package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.attribute.ProductTemplate;
import java.time.format.DateTimeFormatter;

public record ProductTemplateResponse (
        Integer id,
        String name,
        String createdAt,
        String updatedAt
) {
    public static ProductTemplateResponse from(ProductTemplate productTemplate) {
        return new ProductTemplateResponse(
                productTemplate.getId(),
                productTemplate.getName(),
                productTemplate.getCreatedAt() != null ? productTemplate.getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null,
                productTemplate.getUpdatedAt() != null ? productTemplate.getUpdatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null
        );
    }
}
