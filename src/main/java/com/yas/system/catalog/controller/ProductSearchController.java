package com.yas.system.catalog.controller;

import com.yas.system.catalog.internal.dto.request.ProductSearchRequest;
import com.yas.system.catalog.internal.dto.request.ProductSortOption;
import com.yas.system.catalog.internal.dto.response.ProductSearchResultResponse;
import com.yas.system.catalog.internal.dto.response.ProductSuggestionResponse;
import com.yas.system.catalog.internal.dto.response.SearchFacetsResponse;
import com.yas.system.catalog.internal.service.search.ProductSearchService;
import com.yas.system.common.response.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/public/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
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
            @RequestParam(name = "category_id", required = false) Integer categoryIdParam,
            @RequestParam(name = "categoryId", required = false) Integer categoryIdAlt,
            @RequestParam(name = "category_slug", required = false) String categorySlugParam,
            @RequestParam(name = "categorySlug", required = false) String categorySlugAlt,
            @RequestParam(name = "brand_ids", required = false) List<Integer> brandIdsParam,
            @RequestParam(name = "brandIds", required = false) List<Integer> brandIdsAlt,
            @RequestParam(required = false) List<String> attributes,
            @RequestParam(name = "min_price", required = false) BigDecimal minPriceParam,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPriceAlt,
            @RequestParam(name = "max_price", required = false) BigDecimal maxPriceParam,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPriceAlt,
            @RequestParam(defaultValue = "RELEVANCE") ProductSortOption sort,
            @RequestParam(name = "page_number", required = false) Integer pageNumberParam,
            @RequestParam(name = "pageNumber", required = false) Integer pageNumberAlt,
            @RequestParam(name = "page_size", required = false) Integer pageSizeParam,
            @RequestParam(name = "pageSize", required = false) Integer pageSizeAlt
    ) {
        Integer resolvedCategoryId = categoryIdParam != null ? categoryIdParam : categoryIdAlt;
        String resolvedCategorySlug = categorySlugParam != null ? categorySlugParam : categorySlugAlt;
        List<Integer> resolvedBrandIds = brandIdsParam != null ? brandIdsParam : brandIdsAlt;
        BigDecimal resolvedMinPrice = minPriceParam != null ? minPriceParam : minPriceAlt;
        BigDecimal resolvedMaxPrice = maxPriceParam != null ? maxPriceParam : maxPriceAlt;
        int resolvedPageNumber = pageNumberParam != null ? pageNumberParam : (pageNumberAlt != null ? pageNumberAlt : 0);
        int resolvedPageSize = pageSizeParam != null ? pageSizeParam : (pageSizeAlt != null ? pageSizeAlt : 12);

        Map<Long, List<String>> attributeFilters = parseAttributeFilters(attributes);

        ProductSearchRequest searchRequest = new ProductSearchRequest(
                keyword,
                resolvedCategoryId,
                resolvedCategorySlug,
                resolvedBrandIds,
                attributeFilters,
                resolvedMinPrice,
                resolvedMaxPrice,
                sort,
                resolvedPageNumber,
                resolvedPageSize
        );

        return ApiResponse.success(productSearchService.searchProducts(searchRequest));
    }

    @GetMapping("/facets")
    public ApiResponse<SearchFacetsResponse> getFacets(
            @RequestParam(name = "category_id", required = false) Integer categoryIdParam,
            @RequestParam(name = "categoryId", required = false) Integer categoryIdAlt
    ) {
        Integer resolvedCategoryId = categoryIdParam != null ? categoryIdParam : categoryIdAlt;
        return ApiResponse.success(productSearchService.getCategoryFacets(resolvedCategoryId));
    }

    @GetMapping("/category/{categoryId}/facets")
    public ApiResponse<SearchFacetsResponse> getCategoryFacets(
            @PathVariable Integer categoryId
    ) {
        return ApiResponse.success(productSearchService.getCategoryFacets(categoryId));
    }

    private Map<Long, List<String>> parseAttributeFilters(List<String> rawAttributes) {
        if (rawAttributes == null || rawAttributes.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<String>> result = new HashMap<>();
        for (String raw : rawAttributes) {
            String[] parts = raw.split(":", 2);
            if (parts.length == 2) {
                try {
                    Long attrId = Long.parseLong(parts[0].trim());
                    List<String> values = Arrays.stream(parts[1].split(","))
                            .map(String::trim)
                            .filter(s -> !s.isBlank())
                            .toList();
                    if (!values.isEmpty()) {
                        result.computeIfAbsent(attrId, k -> new ArrayList<>()).addAll(values);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Invalid attribute filter format: '{}', expected '<attributeId>:<value1,value2>'", raw);
                }
            }
        }
        return result;
    }
}
