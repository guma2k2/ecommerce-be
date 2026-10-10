package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.option.ProductOption;
import com.yas.system.common.util.DateTimeUtils;

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
                DateTimeUtils.format(option.getCreatedAt()),
                DateTimeUtils.format(option.getUpdatedAt())
        );
    }
}
