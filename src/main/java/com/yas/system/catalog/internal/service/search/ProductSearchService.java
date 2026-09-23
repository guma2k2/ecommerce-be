package com.yas.system.catalog.internal.service.search;

import com.yas.system.catalog.internal.dto.request.ProductSearchRequest;
import com.yas.system.catalog.internal.dto.response.ProductSearchResultResponse;
import com.yas.system.catalog.internal.dto.response.ProductSuggestionResponse;
import com.yas.system.catalog.internal.dto.response.SearchFacetsResponse;

import java.util.List;

public interface ProductSearchService {

    List<ProductSuggestionResponse> getSuggestions(String keyword, int limit);

    ProductSearchResultResponse searchProducts(ProductSearchRequest request);

    SearchFacetsResponse getCategoryFacets(Integer categoryId);
}
