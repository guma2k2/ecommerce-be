package com.yas.system.catalog.internal.dto.response;

import com.yas.system.common.response.PageResponse;

public record ProductSearchResultResponse(
        PageResponse<ProductSearchItemResponse> products,
        SearchFacetsResponse facets
) {
}
