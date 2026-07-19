package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.Brand;
import java.time.format.DateTimeFormatter;

public record BrandResponse(
        Long id,
        String name,
        String description,
        String createdAt,
        String updatedAt
) {
    public static BrandResponse from(Brand brand) {
        return new BrandResponse(
                brand.getId(),
                brand.getName(),
                brand.getDescription(),
                brand.getCreatedAt() != null ? brand.getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null,
                brand.getUpdatedAt() != null ? brand.getUpdatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null
        );
    }
}
