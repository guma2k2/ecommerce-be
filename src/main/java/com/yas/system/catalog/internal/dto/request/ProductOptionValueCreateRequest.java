package com.yas.system.catalog.internal.dto.request;

public record ProductOptionValueCreateRequest(
        String value,
        int position
) {
}
