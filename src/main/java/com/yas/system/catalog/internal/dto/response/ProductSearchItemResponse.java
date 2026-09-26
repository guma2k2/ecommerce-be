package com.yas.system.catalog.internal.dto.response;

import java.math.BigDecimal;

public record ProductSearchItemResponse(
        Long id,
        String name,
        String slug,
        String thumbnailUrl,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String brandName,
        String categoryName,
        Long defaultVariantId
) {
}
