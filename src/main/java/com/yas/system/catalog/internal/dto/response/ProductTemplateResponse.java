package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.attribute.ProductTemplate;
import com.yas.system.common.util.DateTimeUtils;
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
                DateTimeUtils.format(productTemplate.getCreatedAt()),
                DateTimeUtils.format(productTemplate.getUpdatedAt()),
                attributes != null ? attributes : List.of()
        );
    }

    public static ProductTemplateResponse from(ProductTemplate productTemplate) {
        return from(productTemplate, List.of());
    }
}
