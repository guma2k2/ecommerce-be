package com.yas.system.catalog.internal.dto.request;

public record ProductVariantRequest(
        String option1,
        String option2,
        String option3,
        String sku,
        Double price,
        int quality
) {
}
