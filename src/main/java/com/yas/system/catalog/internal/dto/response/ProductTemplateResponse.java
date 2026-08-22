package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.attribute.ProductTemplate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public record ProductTemplateResponse (
        Integer id,
        String name,
        String createdAt,
        String updatedAt,
        List<Long> attributeIds
) {
    public static ProductTemplateResponse from(ProductTemplate productTemplate, List<Long> attributeIds) {
        return new ProductTemplateResponse(
                productTemplate.getId(),
                productTemplate.getName(),
                productTemplate.getCreatedAt() != null ? productTemplate.getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null,
                productTemplate.getUpdatedAt() != null ? productTemplate.getUpdatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null,
                attributeIds != null ? attributeIds : List.of()
        );
    }

    public static ProductTemplateResponse from(ProductTemplate productTemplate) {
        return from(productTemplate, List.of());
    }
}
