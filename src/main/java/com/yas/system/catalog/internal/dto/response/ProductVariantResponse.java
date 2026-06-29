package com.yas.system.catalog.internal.dto.response;

public record ProductVariantResponse(
        Long id,
        String option1,
        String option2,
        String option3,
        String sku,
        Double price,
        int quality
) {
}
