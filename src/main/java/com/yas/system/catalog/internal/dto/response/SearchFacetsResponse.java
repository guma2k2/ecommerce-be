package com.yas.system.catalog.internal.dto.response;

import java.util.List;

public record SearchFacetsResponse(
        List<AttributeFacetResponse> attributes,
        List<BrandFacetResponse> brands,
        PriceRangeFacetResponse priceRange
) {
    public static SearchFacetsResponse empty() {
        return new SearchFacetsResponse(List.of(), List.of(), new PriceRangeFacetResponse(null, null));
    }
}
