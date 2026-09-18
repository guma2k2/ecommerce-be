package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.option.ProductOption;

import java.time.format.DateTimeFormatter;

public record ProductOptionResponse(
        Long id,
        String name,
        String createdAt,
        String updatedAt
) {
    public static ProductOptionResponse from(ProductOption option) {
        return new ProductOptionResponse(
                option.getId(),
                option.getName(),
                option.getCreatedAt() != null ? option.getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null,
                option.getUpdatedAt() != null ? option.getUpdatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null
        );
    }
}
