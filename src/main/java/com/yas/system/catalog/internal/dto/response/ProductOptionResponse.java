package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.ProductOption;

public record ProductOptionResponse(
        Long id,
        String name,
        String[] values,
        int position
) {
    public static ProductOptionResponse from(ProductOption option) {
        return new ProductOptionResponse(
                option.getId(),
                option.getName(),
                option.getValues().toArray(String[]::new),
                option.getPosition()
        );
    }
}
