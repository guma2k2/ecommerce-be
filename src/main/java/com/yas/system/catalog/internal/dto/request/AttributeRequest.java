package com.yas.system.catalog.internal.dto.request;

public record AttributeRequest(
        String name,
        String unit,
        String dataType
) {
}
