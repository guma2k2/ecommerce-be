package com.yas.system.catalog.internal.dto.request;

public record ProductVariantOptionValueUpdateRequest(
        Long productOptionValueId,
        Long productOptionId,
        String value
) {
}
