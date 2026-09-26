package com.yas.system.catalog.internal.dto.request;

import java.math.BigDecimal;
import java.util.List;

public record ProductSearchQuery(
        String keyword,
        Integer categoryId,
        String categorySlug,
        List<Integer> brandIds,
        List<String> attributes,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        ProductSortOption sort,
        Integer pageNumber,
        Integer pageSize
) {
}
