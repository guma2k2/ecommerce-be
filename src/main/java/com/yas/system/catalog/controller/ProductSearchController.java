package com.yas.system.catalog.controller;

import com.yas.system.catalog.internal.dto.request.ProductSearchQuery;
import com.yas.system.catalog.internal.dto.request.ProductSortOption;
import com.yas.system.catalog.internal.dto.response.ProductSearchResultResponse;
import com.yas.system.catalog.internal.dto.response.ProductSuggestionResponse;
import com.yas.system.catalog.internal.dto.response.SearchFacetsResponse;
import com.yas.system.catalog.internal.service.search.ProductSearchService;
import com.yas.system.common.response.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductSearchController {

    ProductSearchService productSearchService;

    @GetMapping("/suggestions")
    public ApiResponse<List<ProductSuggestionResponse>> getSuggestions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ApiResponse.success(productSearchService.getSuggestions(keyword, limit));
    }

    @GetMapping("/search")
    public ApiResponse<ProductSearchResultResponse> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(name = "category_id", required = false) Integer categoryId,
            @RequestParam(name = "category_slug", required = false) String categorySlug,
            @RequestParam(name = "brand_ids", required = false) List<Integer> brandIds,
            @RequestParam(required = false) List<String> attributes,
            @RequestParam(name = "min_price", required = false) BigDecimal minPrice,
            @RequestParam(name = "max_price", required = false) BigDecimal maxPrice,
            @RequestParam(required = false) ProductSortOption sort,
            @RequestParam(name = "page_number", required = false) Integer pageNumber,
            @RequestParam(name = "page_size", required = false) Integer pageSize
    ) {
        ProductSearchQuery query = new ProductSearchQuery(
                keyword,
                categoryId,
                categorySlug,
                brandIds,
                attributes,
                minPrice,
                maxPrice,
                sort,
                pageNumber,
                pageSize
        );

        return ApiResponse.success(productSearchService.searchProducts(query));
    }

    @GetMapping("/facets")
    public ApiResponse<SearchFacetsResponse> getFacets(
            @RequestParam String keyword,
            @RequestParam(name = "category_id", required = false) Integer categoryId
    ) {
        return ApiResponse.success(productSearchService.getFacets(keyword, categoryId));
    }
}
