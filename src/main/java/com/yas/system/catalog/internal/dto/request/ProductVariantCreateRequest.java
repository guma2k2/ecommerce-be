package com.yas.system.catalog.internal.dto.request;

import java.util.Map;

public record ProductVariantCreateRequest (
        Double price,
        Integer quantity,
        Map<String, String> attributes
) {
}
