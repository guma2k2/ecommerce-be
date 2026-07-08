package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.ProductOption;

import java.util.List;

public record ProductOptionResponse(
        Long id,
        String name,
        List<String> values,
        int position
) {
    public static ProductOptionResponse from(ProductOption option) {
        return new ProductOptionResponse(
                option.getId(),
                option.getName(),
                option.getValues(),
                option.getPosition()
        );
    }
}
