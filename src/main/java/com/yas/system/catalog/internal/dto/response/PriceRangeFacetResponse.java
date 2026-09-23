package com.yas.system.catalog.internal.dto.response;

import java.math.BigDecimal;

public record PriceRangeFacetResponse(
        BigDecimal minPrice,
        BigDecimal maxPrice
) {
}
