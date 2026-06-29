package com.yas.system.catalog.internal.dto.response;

public record ProductOptionResponse(
        Long id,
        String name,
        String[] values,
        int position
) {
}
