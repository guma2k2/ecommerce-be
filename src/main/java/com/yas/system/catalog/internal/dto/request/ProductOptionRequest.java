package com.yas.system.catalog.internal.dto.request;

public record ProductOptionRequest (
        String name,
        String[] values,
        int position
) {
}
