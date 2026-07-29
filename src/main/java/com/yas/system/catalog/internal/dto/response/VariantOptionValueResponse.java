package com.yas.system.catalog.internal.dto.response;

public record VariantOptionValueResponse(
        Long id, // product option id
        String value,
        int position
) {
}
