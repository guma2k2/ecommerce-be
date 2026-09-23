package com.yas.system.catalog.internal.service.search.impl;

import com.yas.system.catalog.internal.dto.request.ProductSearchRequest;
import com.yas.system.catalog.internal.dto.request.ProductSortOption;
import com.yas.system.catalog.internal.dto.response.*;
import com.yas.system.catalog.internal.entity.Category;
import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.ProductMedia;
import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import com.yas.system.catalog.internal.repository.CategoryRepository;
import com.yas.system.catalog.internal.repository.ProductMediaRepository;
import com.yas.system.catalog.internal.repository.ProductSearchRepository;
import com.yas.system.catalog.internal.repository.ProductVariantRepository;
import com.yas.system.catalog.internal.repository.projection.AttributeFacetProjection;
import com.yas.system.catalog.internal.repository.projection.BrandFacetProjection;
import com.yas.system.catalog.internal.repository.projection.PriceRangeProjection;
import com.yas.system.catalog.internal.repository.projection.ProductSuggestionProjection;
import com.yas.system.catalog.internal.service.search.ProductSearchService;
import com.yas.system.catalog.internal.specification.ProductSearchSpecification;
import com.yas.system.common.response.PageResponse;
import com.yas.system.media.api.MediaPublicService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "app.search.provider", havingValue = "postgres", matchIfMissing = true)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PostgreSqlProductSearchServiceImpl implements ProductSearchService {

    ProductSearchRepository productSearchRepository;
    CategoryRepository categoryRepository;
    ProductMediaRepository productMediaRepository;
    ProductVariantRepository productVariantRepository;
    MediaPublicService mediaPublicService;

    @Override
    @Transactional(readOnly = true)
    public List<ProductSuggestionResponse> getSuggestions(String keyword, int limit) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        int queryLimit = Math.min(Math.max(limit, 1), 20);
        List<ProductSuggestionProjection> projections = productSearchRepository.findSuggestions(keyword.trim(), queryLimit);

        if (projections.isEmpty()) {
            return List.of();
        }

        List<String> mediaIds = projections.stream()
                .map(ProductSuggestionProjection::getMediaId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<String, String> mediaUrls = mediaIds.isEmpty() ? Map.of() : mediaPublicService.getMediaUrls(mediaIds);

        return projections.stream()
                .map(p -> new ProductSuggestionResponse(
                        p.getId(),
                        p.getName(),
                        p.getSlug(),
                        p.getMediaId() != null ? mediaUrls.get(p.getMediaId()) : null,
                        p.getMinPrice(),
                        p.getCategoryName()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchFacetsResponse getCategoryFacets(Integer categoryId) {
        if (categoryId == null) {
            return SearchFacetsResponse.empty();
        }

        List<Integer> categoryIds = categoryRepository.findCategoryAndDescendantIds(categoryId);
        return buildFacets(categoryIds, true);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductSearchResultResponse searchProducts(ProductSearchRequest request) {
        // Step 1: Resolve category and descendant subcategory IDs
        List<Integer> categoryIds = resolveCategoryIds(request);

        // Step 2: Build pagination and sort
        Sort sort = switch (request.sort() != null ? request.sort() : ProductSortOption.RELEVANCE) {
            case NEWEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            case PRICE_ASC, PRICE_DESC, RELEVANCE -> Sort.by(Sort.Direction.DESC, "id");
        };
        Pageable pageable = PageRequest.of(request.pageNumber(), request.pageSize(), sort);

        // Step 3: Build dynamic JPA specification and query products
        Specification<Product> spec = ProductSearchSpecification.buildSpecification(request, categoryIds);
        Page<Product> productPage = productSearchRepository.findAll(spec, pageable);

        // Step 4: Map paginated products with thumbnails and prices
        List<ProductSearchItemResponse> searchItems = mapProductSearchItems(productPage.getContent());

        PageResponse<ProductSearchItemResponse> pageResponse = new PageResponse<>(
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalPages(),
                productPage.getTotalElements(),
                searchItems
        );

        // Step 5: Build facets (attributes, brands, price range)
        boolean hasCategoryFilter = !categoryIds.isEmpty();
        SearchFacetsResponse facets = buildFacets(categoryIds, hasCategoryFilter);

        return new ProductSearchResultResponse(pageResponse, facets);
    }

    private List<Integer> resolveCategoryIds(ProductSearchRequest request) {
        if (request.categoryId() != null) {
            return categoryRepository.findCategoryAndDescendantIds(request.categoryId());
        }
        if (request.categorySlug() != null && !request.categorySlug().isBlank()) {
            return categoryRepository.findByName(request.categorySlug())
                    .map(Category::getId)
                    .map(categoryRepository::findCategoryAndDescendantIds)
                    .orElse(List.of());
        }
        return List.of();
    }

    private List<ProductSearchItemResponse> mapProductSearchItems(List<Product> products) {
        if (products.isEmpty()) {
            return List.of();
        }

        List<Long> productIds = products.stream()
                .map(Product::getId)
                .toList();

        // Batch resolve primary media
        List<ProductMedia> medias = productMediaRepository.findByProductIdInOrderByPositionAsc(productIds);
        Map<Long, String> primaryMediaIdByProductId = medias.stream()
                .collect(Collectors.groupingBy(
                        pm -> pm.getProduct().getId(),
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.isEmpty() ? null : list.getFirst().getMediaId()
                        )
                ));

        List<String> allMediaIds = primaryMediaIdByProductId.values().stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<String, String> mediaUrls = allMediaIds.isEmpty() ? Map.of() : mediaPublicService.getMediaUrls(allMediaIds);

        // Batch resolve variant prices (minPrice, maxPrice)
        List<ProductVariant> variants = productVariantRepository.findByProductIdIn(productIds);
        Map<Long, List<ProductVariant>> variantsByProductId = variants.stream()
                .collect(Collectors.groupingBy(pv -> pv.getProduct().getId()));

        return products.stream()
                .map(product -> {
                    String mediaId = primaryMediaIdByProductId.get(product.getId());
                    String thumbnailUrl = mediaId != null ? mediaUrls.get(mediaId) : null;

                    List<ProductVariant> productVariants = variantsByProductId.getOrDefault(product.getId(), List.of());
                    BigDecimal minPrice = productVariants.stream()
                            .map(ProductVariant::getPrice)
                            .filter(Objects::nonNull)
                            .min(BigDecimal::compareTo)
                            .orElse(null);
                    BigDecimal maxPrice = productVariants.stream()
                            .map(ProductVariant::getPrice)
                            .filter(Objects::nonNull)
                            .max(BigDecimal::compareTo)
                            .orElse(null);

                    String brandName = product.getBrand() != null ? product.getBrand().getName() : null;
                    String categoryName = product.getCategory() != null ? product.getCategory().getName() : null;

                    return new ProductSearchItemResponse(
                            product.getId(),
                            product.getName(),
                            product.getSlug(),
                            thumbnailUrl,
                            minPrice,
                            maxPrice,
                            brandName,
                            categoryName
                    );
                })
                .toList();
    }

    private SearchFacetsResponse buildFacets(List<Integer> categoryIds, boolean hasCategories) {
        List<Integer> effectiveCategoryIds = categoryIds.isEmpty() ? List.of(0) : categoryIds;

        // 1. Attribute facets
        List<AttributeFacetProjection> attrProjections = productSearchRepository.findAttributeFacets(effectiveCategoryIds, hasCategories);
        Map<Long, List<AttributeFacetProjection>> groupedByAttribute = attrProjections.stream()
                .collect(Collectors.groupingBy(
                        AttributeFacetProjection::getAttributeId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<AttributeFacetResponse> attributeFacets = groupedByAttribute.entrySet().stream()
                .map(entry -> {
                    Long attrId = entry.getKey();
                    List<AttributeFacetProjection> projs = entry.getValue();
                    String attrName = projs.getFirst().getAttributeName();
                    List<FacetValueResponse> values = projs.stream()
                            .map(p -> new FacetValueResponse(p.getAttributeValue(), p.getProductCount()))
                            .toList();
                    return new AttributeFacetResponse(attrId, attrName, values);
                })
                .toList();

        // 2. Brand facets
        List<BrandFacetProjection> brandProjections = productSearchRepository.findBrandFacets(effectiveCategoryIds, hasCategories);
        List<BrandFacetResponse> brandFacets = brandProjections.stream()
                .map(p -> new BrandFacetResponse(p.getBrandId(), p.getBrandName(), p.getProductCount()))
                .toList();

        // 3. Price range facet
        PriceRangeProjection priceRangeProj = productSearchRepository.findPriceRange(effectiveCategoryIds, hasCategories);
        PriceRangeFacetResponse priceRange = priceRangeProj != null
                ? new PriceRangeFacetResponse(priceRangeProj.getMinPrice(), priceRangeProj.getMaxPrice())
                : new PriceRangeFacetResponse(null, null);

        return new SearchFacetsResponse(attributeFacets, brandFacets, priceRange);
    }
}
