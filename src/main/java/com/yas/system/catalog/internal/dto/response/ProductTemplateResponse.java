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
        List<ProductAttributeResponse> attributes
) {
    public static ProductTemplateResponse from(ProductTemplate productTemplate, List<ProductAttributeResponse> attributes) {
        return new ProductTemplateResponse(
                productTemplate.getId(),
                productTemplate.getName(),
                productTemplate.getCreatedAt() != null ? productTemplate.getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null,
                productTemplate.getUpdatedAt() != null ? productTemplate.getUpdatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null,
                attributes != null ? attributes : List.of()
        );
    }

    public static ProductTemplateResponse from(ProductTemplate productTemplate) {
        return from(productTemplate, List.of());
    }
}
