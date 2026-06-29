package com.yas.system.catalog.internal.dto.response;

public record ProductResponse(
        Long id,
        String name,
        String description,
        String slug,
        ProductOptionResponse[] options,
        ProductVariantResponse[] variants
) {
}
