package com.yas.system.catalog.internal.dto.request;

public record ProductOptionValueUpdateRequest(
        Long id, // ProductOptionValue ID
        String value,
        int position
) {
}
