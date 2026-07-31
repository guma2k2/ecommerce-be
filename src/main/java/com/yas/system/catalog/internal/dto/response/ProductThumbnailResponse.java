package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.Product;

import java.time.format.DateTimeFormatter;

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
                null,
                product.getCreatedAt() != null ? product.getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null,
                product.getUpdatedAt() != null ? product.getUpdatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null
        );
    }
}
