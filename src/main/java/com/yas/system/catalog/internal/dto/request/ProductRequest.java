package com.yas.system.catalog.internal.dto.request;

public record ProductRequest(
        String name,
        String description,
        String slug,
        ProductVariantRequest[] variants,
        ProductOptionRequest[] options
) {
}
