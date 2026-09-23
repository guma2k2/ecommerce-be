package com.yas.system.catalog.internal.dto.response;

import java.math.BigDecimal;

public record ProductSuggestionResponse(
        Long id,
        String name,
        String slug,
        String thumbnailUrl,
        BigDecimal price,
        String categoryName
) {
}
