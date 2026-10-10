package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.Brand;
import com.yas.system.common.util.DateTimeUtils;

public record BrandResponse(
        Integer id,
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
                DateTimeUtils.format(brand.getCreatedAt()),
                DateTimeUtils.format(brand.getUpdatedAt())
        );
    }
}
