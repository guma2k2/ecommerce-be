package com.yas.system.catalog.internal.dto.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ProductSearchRequest(
        String keyword,
        Integer categoryId,
        String categorySlug,
        List<Integer> brandIds,
        Map<Long, List<String>> attributeFilters,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        ProductSortOption sort,
        Integer pageNumber,
        Integer pageSize
) {
    public ProductSearchRequest {
        if (pageNumber == null || pageNumber < 0) {
            pageNumber = 0;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = 12;
        }
        if (sort == null) {
            sort = ProductSortOption.RELEVANCE;
        }
    }
}
