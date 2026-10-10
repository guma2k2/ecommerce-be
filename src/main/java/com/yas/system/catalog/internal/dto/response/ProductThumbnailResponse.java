package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.common.util.DateTimeUtils;

public record ProductThumbnailResponse (
        Long id,
        String name,
        String status,
        String createdAt,
        String updatedAt
) {
    public static ProductThumbnailResponse from(Product product) {
        return new ProductThumbnailResponse(
                product.getId(),
                product.getName(),
                product.getStatus().name(),
                DateTimeUtils.format(product.getCreatedAt()),
                DateTimeUtils.format(product.getUpdatedAt())
        );
    }
}
